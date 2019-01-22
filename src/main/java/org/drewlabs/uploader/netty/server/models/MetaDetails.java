package org.drewlabs.uploader.netty.server.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "meta-data") // not getting in use
public class MetaDetails {

    @JsonProperty(value = "file-name")
    private String filename;

    @JsonProperty(value = "size")
    private String fileSize;

    @JsonProperty(value = "image-type")
    private String imageEncoding;

    public String getImageEncoding() {
        return imageEncoding;
    }

    public void setImageEncoding(String imageEncoding) {
        this.imageEncoding = imageEncoding;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "{" +
                "filename='" + filename + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", imageEncoding='" + imageEncoding + '\'' +
                '}';
    }

}
//        inBuffer.readBytes(imageByte);
//        String imageBytes = new String(imageByte);
//        imageFile += imageBytes;
//        if(Boolean.FALSE.equals(isMetaReceived)) {
//            if (imageBytes.contains("meta-data") & imageBytes.contains(CONNECTION_CLOSE_COMMAND)){
//                System.out.println("Connected channel history is : " + ctx.channel());
//                isMetaReceived = true;
//                appendFileMetaData(imageBytes);
//                writeSmallSizedImage();
//                flushTheConnectionStream(ctx);
//                return;
//            }else if(imageBytes.contains("meta-data")){
//                isMetaReceived = true;
//                System.out.println("Connected channel history is : " + ctx.channel());
//                appendFileMetaData(imageBytes);
//            }
//        }
//        if(imageBytes.contains(CONNECTION_CLOSE_COMMAND)){ // add case where image_streaming_close come in the base encoding, just in case
//            writeImageWithMetadataValidation();
//            flushTheConnectionStream(ctx);
//        }