/**
 * This class only implements read operations, no write operations are done from this handler
 * Files are uniquely identified by remote ip address and port combination (/0.0.0.0<port>)
 */
package org.drewlabs.uploader.netty.server.handler;

import org.drewlabs.uploader.netty.cache.FileCache;
import org.drewlabs.uploader.netty.writer.FileWrite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Controller
public class WriteBytesHandler extends ChannelInboundHandlerAdapter {

    public static Logger logger = Logger.getLogger(WriteBytesHandler.class.toString());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf inBuffer = (ByteBuf) msg;
        byte[] packetBytes = new byte[inBuffer.readableBytes()];
        inBuffer.readBytes(packetBytes);
        Channel channel = ctx.channel();
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        String remoteIp = socketAddress.getAddress().toString();
        int remotePort = socketAddress.getPort();
        String processId = remoteIp+remotePort;
        System.out.println("Request from " + processId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] previousBytes = FileCache.fileBytesMap.remove(processId);
        if(previousBytes == null){
            baos.write(packetBytes);
        }else {
            baos.write(previousBytes);
            baos.write(packetBytes);
        }
        FileCache.fileBytesMap.put(processId, baos.toByteArray());
        System.out.println("Putter byte buffer of length :: " + baos.toByteArray().length);
        int originalSize = FileCache.fileSize.get(processId);
        if(originalSize == baos.toByteArray().length){
            System.out.println("Going to write the file of size " + originalSize + " B");
            // write file from byte array
            if(FileWrite.writeFileFromByteArray(baos.toByteArray(), FileCache.fileNameMap.get(processId))){
                // remove all the things related to this process id
                FileCache.fileBytesMap.remove(processId);
                FileCache.fileSize.remove(processId);
                FileCache.fileNameMap.remove(processId);
            }else{
                logger.log(Level.WARNING, "Reached to unknown state");
            }
        }
        baos.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }
}
