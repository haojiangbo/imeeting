package com.haojiangbo.utils;


import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;

/**
 *
 * https://blog.csdn.net/chenhande1990chenhan/article/details/88353271
 * 功能：YV12转其他格式
 * 作者：chenhan
 * 时间：2019-3-7 16：47
 */
/**
 * yuv420p:  yyyyyyyyuuvv
 * yuv420sp: yyyyyyyyuvuv
 * nv21:     yyyyyyyyvuvu
 */

public class ImageUtil {
  public static final int YUV420P = 0;
    public static final int YUV420SP = 1;
    public static final int NV21 = 2;
    private static final String TAG = "ImageUtil";

//    /***
//     * 此方法内注释以640*480为例
//     * 未考虑CropRect的
//     */
//    public static byte[] getBytesFromImageAsType(Image image, int type) {
//        try {
//            //获取源数据，如果是YUV格式的数据planes.length = 3
//            //plane[i]里面的实际数据可能存在byte[].length <= capacity (缓冲区总大小)
//            final Image.Plane[] planes = image.getPlanes();
//
//            //数据有效宽度，一般的，图片width <= rowStride，这也是导致byte[].length <= capacity的原因
//            // 所以我们只取width部分
//            int width = image.getWidth();
//            int height = image.getHeight();
//
//            //此处用来装填最终的YUV数据，需要1.5倍的图片大小，因为Y U V 比例为 4:1:1
//            byte[] yuvBytes = new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.YV12) / 8];
//            //目标数组的装填到的位置
//            int dstIndex = 0;
//
//            //临时存储uv数据的
//            byte uBytes[] = new byte[width * height / 4];
//            byte vBytes[] = new byte[width * height / 4];
//            int uIndex = 0;
//            int vIndex = 0;
//
//            int pixelsStride, rowStride;
//            for (int i = 0; i < planes.length; i++) {
//                pixelsStride = planes[i].getPixelStride();
//                rowStride = planes[i].getRowStride();
//
//                ByteBuffer buffer = planes[i].getBuffer();
//
//                //如果pixelsStride==2，一般的Y的buffer长度=640*480，UV的长度=640*480/2-1
//                //源数据的索引，y的数据是byte中连续的，u的数据是v向左移以为生成的，两者都是偶数位为有效数据
//                byte[] bytes = new byte[buffer.capacity()];
//                buffer.get(bytes);
//
//                int srcIndex = 0;
//                if (i == 0) {
//                    //直接取出来所有Y的有效区域，也可以存储成一个临时的bytes，到下一步再copy
//                    for (int j = 0; j < height; j++) {
//                        System.arraycopy(bytes, srcIndex, yuvBytes, dstIndex, width);
//                        srcIndex += rowStride;
//                        dstIndex += width;
//                    }
//                } else if (i == 1) {
//                    //根据pixelsStride取相应的数据
//                    for (int j = 0; j < height / 2; j++) {
//                        for (int k = 0; k < width / 2; k++) {
//                            uBytes[uIndex++] = bytes[srcIndex];
//                            srcIndex += pixelsStride;
//                        }
//                        if (pixelsStride == 2) {
//                            srcIndex += rowStride - width;
//                        } else if (pixelsStride == 1) {
//                            srcIndex += rowStride - width / 2;
//                        }
//                    }
//                } else if (i == 2) {
//                    //根据pixelsStride取相应的数据
//                    for (int j = 0; j < height / 2; j++) {
//                        for (int k = 0; k < width / 2; k++) {
//                            vBytes[vIndex++] = bytes[srcIndex];
//                            srcIndex += pixelsStride;
//                        }
//                        if (pixelsStride == 2) {
//                            srcIndex += rowStride - width;
//                        } else if (pixelsStride == 1) {
//                            srcIndex += rowStride - width / 2;
//                        }
//                    }
//                }
//            }
//
//            image.close();
//
//            //根据要求的结果类型进行填充
//            switch (type) {
//                case YUV420P:
//                    System.arraycopy(uBytes, 0, yuvBytes, dstIndex, uBytes.length);
//                    System.arraycopy(vBytes, 0, yuvBytes, dstIndex + uBytes.length, vBytes.length);
//                    break;
//                case YUV420SP:
//                    for (int i = 0; i < vBytes.length; i++) {
//                        yuvBytes[dstIndex++] = uBytes[i];
//                        yuvBytes[dstIndex++] = vBytes[i];
//                    }
//                    break;
//                case NV21:
//                    for (int i = 0; i < vBytes.length; i++) {
//                        yuvBytes[dstIndex++] = vBytes[i];
//                        yuvBytes[dstIndex++] = uBytes[i];
//                    }
//                    break;
//            }
//            return yuvBytes;
//        } catch (final Exception e) {
//            if (image != null) {
//                image.close();
//            }
//        }
//        return null;
//    }

    public static final int COLOR_FormatI420 = 1;
    public static final int COLOR_FormatNV21 = 2;
    public static final boolean VERBOSE = true;
    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }

    public static byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        if (VERBOSE) Log.w(TAG, "get data from " + planes.length + " planes");
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            if (VERBOSE) {
                Log.w(TAG, "pixelStride " + pixelStride);
                Log.w(TAG, "rowStride " + rowStride);
                Log.w(TAG, "width " + width);
                Log.w(TAG, "height " + height);
                Log.w(TAG, "buffer size " + buffer.remaining());
            }
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
            if (VERBOSE) Log.w(TAG, "Finished reading data from plane " + i);
        }
        return data;
    }


    public static byte[] getDataFromImageByHaojiangbo(Image image) {

        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        int userIndex = 0;
        for(int i = 0; i < planes.length; i++){
            Image.Plane plane = planes[i];
            ByteBuffer byteBuf =  plane.getBuffer();
            switch (i){
                case 0:
                    byteBuf.get(data,0,width * height);
                    userIndex += width * height;
                    break;
                case 1:
                case 2:
                    for(int z = 0; z < width / 2 * height - 1; z += 2){
                        data [userIndex]  =  byteBuf.get(z);
                        userIndex ++;
                    }
            }
        }
        return data;
    }






}
