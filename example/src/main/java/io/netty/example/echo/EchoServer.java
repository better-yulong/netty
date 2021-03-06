/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.example.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Echoes back any received data from a client.
 */
public final class EchoServer {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        final EchoServerHandler serverHandler = new EchoServerHandler();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_BACKLOG, 100)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                     if (sslCtx != null) {
                         p.addLast(sslCtx.newHandler(ch.alloc()));
                     }
                     p.addLast(new LoggingHandler(LogLevel.INFO));
                     p.addLast(serverHandler);
                 }
             });

            // Start the server.
            ChannelFuture f = b.bind(PORT).sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
/**
客户端请求后服务接收到“Hello, Netty”日志如下：
18:17:02.052 [nioEventLoopGroup-2-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xcf89a579] REGISTERED
18:17:02.055 [nioEventLoopGroup-2-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xcf89a579] BIND: 0.0.0.0/0.0.0.0:8007
18:17:02.057 [nioEventLoopGroup-2-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xcf89a579, L:/0:0:0:0:0:0:0:0:8007] ACTIVE
18:17:13.213 [nioEventLoopGroup-2-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xcf89a579, L:/0:0:0:0:0:0:0:0:8007] READ: [id: 0xd149e4f5, L:/127.0.0.1:8007 - R:/127.0.0.1:58220]
18:17:13.214 [nioEventLoopGroup-2-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xcf89a579, L:/0:0:0:0:0:0:0:0:8007] READ COMPLETE
18:17:13.247 [nioEventLoopGroup-3-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xd149e4f5, L:/127.0.0.1:8007 - R:/127.0.0.1:58220] REGISTERED
18:17:13.247 [nioEventLoopGroup-3-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xd149e4f5, L:/127.0.0.1:8007 - R:/127.0.0.1:58220] ACTIVE
18:17:13.257 [nioEventLoopGroup-3-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xd149e4f5, L:/127.0.0.1:8007 - R:/127.0.0.1:58220] READ: 12B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 48 65 6c 6c 6f 2c 20 4e 65 74 74 79             |Hello, Netty    |
+--------+-------------------------------------------------+----------------+
Server received：Hello, Netty
18:17:13.257 [nioEventLoopGroup-3-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xd149e4f5, L:/127.0.0.1:8007 - R:/127.0.0.1:58220] WRITE: 12B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 48 65 6c 6c 6f 2c 20 4e 65 74 74 79             |Hello, Netty    |
+--------+-------------------------------------------------+----------------+
18:17:13.258 [nioEventLoopGroup-3-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xd149e4f5, L:/127.0.0.1:8007 - R:/127.0.0.1:58220] READ COMPLETE
18:17:13.258 [nioEventLoopGroup-3-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xd149e4f5, L:/127.0.0.1:8007 - R:/127.0.0.1:58220] FLUSH
18:17:13.259 [nioEventLoopGroup-3-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xd149e4f5, L:/127.0.0.1:8007 - R:/127.0.0.1:58220] READ COMPLETE
18:17:13.259 [nioEventLoopGroup-3-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xd149e4f5, L:/127.0.0.1:8007 - R:/127.0.0.1:58220] FLUSH
18:17:13.259 [nioEventLoopGroup-3-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xd149e4f5, L:/127.0.0.1:8007 ! R:/127.0.0.1:58220] INACTIVE
18:17:13.259 [nioEventLoopGroup-3-1] INFO  i.n.handler.logging.LoggingHandler - [id: 0xd149e4f5, L:/127.0.0.1:8007 ! R:/127.0.0.1:58220] UNREGISTERED

*/
