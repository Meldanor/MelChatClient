/*
 * Copyright (C) 2013 Kilian Gaertner
 * 
 * This file is part of MelChatServer.
 * 
 * MelChatServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * MelChatServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MelChatServer.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.meldanor.melchatclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;

import com.ronsoft.SystemInPipe;

public class NetworkHandler {

    private Selector selector;

    private SocketChannel socketChannel;
    private SelectableChannel stdIn;

    public NetworkHandler(String host, String port) throws Exception {
        createConnection(host, port);
    }

    private void createConnection(String host, String port) throws Exception {

        selector = Selector.open();
        // Connect to the server
        socketChannel = SocketChannel.open(new InetSocketAddress(host, Integer.parseInt(port)));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        // Wrapper for console input
        SystemInPipe stdinPipe = new SystemInPipe();

        stdIn = stdinPipe.getStdinChannel();
        stdinPipe.start();
        stdIn.register(selector, SelectionKey.OP_READ);
    }

    private boolean clientIsRunning = true;

    public void clientLoop() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
        try {
            while (clientIsRunning) {
                int rdyChannels = selector.select(0);
                if (rdyChannels == 0)
                    continue;

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    buffer.clear();
                    if (key.isReadable()) {
                        // Server want something
                        if (key.channel() instanceof SocketChannel) {
                            SocketChannel sockChannel = (SocketChannel) key.channel();
                            try {
                                sockChannel.read(buffer);
                                buffer.flip();
                                WritableByteChannel stdout = Channels.newChannel(System.out);
                                stdout.write(buffer);
                            } catch (IOException IOE) {
                                clientIsRunning = false;
                                System.out.println("Server closed connection");
                            }

                        }
                        // Client want something
                        else {
                            ReadableByteChannel channel = (ReadableByteChannel) stdIn;
                            channel.read(buffer);
                            buffer.flip();
                            socketChannel.write(buffer);
                        }
                    }
                    it.remove();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
