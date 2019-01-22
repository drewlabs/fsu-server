package org.drewlabs.uploader.netty.server.handler;

import org.drewlabs.uploader.netty.server.models.MetaDetails;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final String CONNECTION_CLOSE_COMMAND = "image_streaming_close";
    private String imageFile = "";
    private static HashMap<String, byte[]> fileBytesMap = new HashMap<>();
    private static HashMap<String, Integer[]> fileWHDetailsMap = new HashMap<>();
    private static HashMap<String, String> fileNameMap = new HashMap<>();
    private boolean isMetaReceived = false;
    private int PROCESS_ID_LENGTH = "PROCESS_ID".length();
    private MetaDetails metaDetails = new MetaDetails();
    private static final String META_DATA_ACKNOWLEDGE = "META_DATA_RECEIVING";
    private static final String CLOSE_CONNECTION = "CLOSE_CONNECTION";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // FIXME :: 1. handle case of second packet read when one is on the process 2. Image could have meta-data base encoding in it, use isMetaReceived to validate on words
        ByteBuf inBuffer = (ByteBuf) msg;
        byte[] packetBytes = new byte[inBuffer.readableBytes()];
        inBuffer.readBytes(packetBytes);
//        System.out.println("Bits are :: " + Integer.toString(packetBytes.length) + " and array is"+Arrays.toString(packetBytes));
        int metaByteLength = META_DATA_ACKNOWLEDGE.length();
        byte[] metaBytes = new byte[metaByteLength];
        System.arraycopy(packetBytes, 0, metaBytes, 0, metaByteLength);
        String expectedMetaStream = new String(metaBytes);
        if(expectedMetaStream.equals(META_DATA_ACKNOWLEDGE)){
            Integer widthL = (int) packetBytes[metaByteLength];
            Integer heightL = (int) packetBytes[metaByteLength+1];
            System.out.println("width and height length is " + widthL + " " + heightL);
            Integer width = Integer.parseInt(new String(Arrays.copyOfRange(packetBytes, metaByteLength+2, metaByteLength+widthL+2)));
            Integer height = Integer.parseInt(new String(Arrays.copyOfRange(packetBytes, metaByteLength+widthL+2, metaByteLength+widthL+heightL+2)));
            String processId = new String(Arrays.copyOfRange(packetBytes,metaByteLength+widthL+heightL+3, (int)packetBytes[metaByteLength+widthL+heightL+2]+ metaByteLength+widthL+heightL+3));
            String fileName = new String(Arrays.copyOfRange(packetBytes, (int)packetBytes[metaByteLength+widthL+heightL+2]+ metaByteLength+widthL+heightL+3, packetBytes.length));
            fileWHDetailsMap.put(processId, new Integer[]{width, height});
            byte[] sampleBytes = new byte[]{1};
            fileNameMap.put(processId, fileName);
            fileBytesMap.put(processId, sampleBytes);
            System.out.println(processId + Arrays.toString(sampleBytes));
            // file name and process id is still have to be identifies TODO
            System.out.println("Metadata received " + width +" and height " + height);
            System.out.println("Process id is " + processId);
            System.out.println("Meta Packet details are " + Arrays.toString(packetBytes));
            writePongBackToClient(ctx);
            return;
        }
        int messageCloseLength = CLOSE_CONNECTION.length();
        byte[] closeMessageBytes = new byte[messageCloseLength];
        System.arraycopy(packetBytes, 0, closeMessageBytes, 0, messageCloseLength);
        String expectedCloseMessage = new String(closeMessageBytes);
        if(expectedCloseMessage.equals(CLOSE_CONNECTION)){
            String processIdToWhichClose = new String(Arrays.copyOfRange(packetBytes, messageCloseLength, packetBytes.length));
            System.out.println("Image has received for the process " + processIdToWhichClose + " close relevant connection :)");
            System.out.println("Received image bytes are :: " + fileBytesMap.get(processIdToWhichClose).length);
            System.out.println("Close connection packet is " + Arrays.toString(packetBytes));
            writeImageFromByteArray(processIdToWhichClose);
            writePongBackToClient(ctx);
            // TODO :: close the connection
            return;
        }
        String processId = new String(Arrays.copyOfRange(packetBytes, 0, PROCESS_ID_LENGTH));
        writeFileBytes(processId, Arrays.copyOfRange(packetBytes, PROCESS_ID_LENGTH, packetBytes.length));
//        System.out.println("Last byte details are "+ packetBytes.length + " and bytes are"+ Arrays.toString(packetBytes) );
        writePongBackToClient(ctx);
        System.out.println("Sending response back");
    }
    private void writePongBackToClient(ChannelHandlerContext ctx) throws Exception {
        ChannelOutbound outbound = new ChannelOutbound();
        ChannelFuture cf = ctx.writeAndFlush(Unpooled.copiedBuffer("PONG", CharsetUtil.UTF_8));
        ChannelPromise promise = new ChannelPromise() {
            @Override
            public Channel channel() {
                System.out.println("Writing pong man");
                return null;
            }

            @Override
            public ChannelPromise setSuccess(Void aVoid) {
                return null;
            }

            @Override
            public ChannelPromise setSuccess() {
                return null;
            }

            @Override
            public boolean trySuccess() {
                return false;
            }

            @Override
            public ChannelPromise setFailure(Throwable throwable) {
                return null;
            }

            @Override
            public ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> genericFutureListener) {
                return null;
            }

            @Override
            public ChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... genericFutureListeners) {
                return null;
            }

            @Override
            public ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> genericFutureListener) {
                return null;
            }

            @Override
            public ChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... genericFutureListeners) {
                return null;
            }

            @Override
            public ChannelPromise sync() throws InterruptedException {
                return null;
            }

            @Override
            public ChannelPromise syncUninterruptibly() {
                return null;
            }

            @Override
            public ChannelPromise await() throws InterruptedException {
                return null;
            }

            @Override
            public ChannelPromise awaitUninterruptibly() {
                return null;
            }

            @Override
            public ChannelPromise unvoid() {
                return null;
            }

            @Override
            public boolean isVoid() {
                return false;
            }

            @Override
            public boolean trySuccess(Void aVoid) {
                return false;
            }

            @Override
            public boolean tryFailure(Throwable throwable) {
                return false;
            }

            @Override
            public boolean setUncancellable() {
                return false;
            }

            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public boolean isCancellable() {
                return false;
            }

            @Override
            public Throwable cause() {
                return null;
            }

            @Override
            public boolean await(long l, TimeUnit timeUnit) throws InterruptedException {
                return false;
            }

            @Override
            public boolean await(long l) throws InterruptedException {
                return false;
            }

            @Override
            public boolean awaitUninterruptibly(long l, TimeUnit timeUnit) {
                return false;
            }

            @Override
            public boolean awaitUninterruptibly(long l) {
                return false;
            }

            @Override
            public Void getNow() {
                return null;
            }

            @Override
            public boolean cancel(boolean b) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public Void get() throws InterruptedException, ExecutionException {
                return null;
            }

            @Override
            public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };
        outbound.write(ctx, Unpooled.copiedBuffer("PONG", CharsetUtil.UTF_8), promise);
        System.out.println(cf.isDone());
    }

    private void writeFileBytes(String processId, byte[] newBytes) throws IOException {
        byte[] previousBytes = fileBytesMap.get(processId);
        if(previousBytes==null){
            previousBytes = new byte[]{1};
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(previousBytes);
        outputStream.write(newBytes);
        fileBytesMap.put(processId, outputStream.toByteArray());
    }
    private void writeImageFromByteArray(String processId) throws IOException {
        System.out.println("Start wiritng file");
        byte[] imagePixels = fileBytesMap.get(processId);
        System.out.println(imagePixels.length);
        Integer[] wh = fileWHDetailsMap.get(processId);
        File file = new File("/Users/nirmalsarswat/Desktop/tmp/" + fileNameMap.get(processId));
        ByteArrayOutputStream btOutputStream = new ByteArrayOutputStream();
        btOutputStream.write(Arrays.copyOfRange(imagePixels, 1, imagePixels.length));
        OutputStream outputStream = new FileOutputStream(file);
        btOutputStream.writeTo(outputStream);
        System.out.println("I have written the image :)");
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
