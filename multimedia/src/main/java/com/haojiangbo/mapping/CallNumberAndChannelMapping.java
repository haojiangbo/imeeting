package com.haojiangbo.mapping;

import com.haojiangbo.protocol.RoomInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
  * <p>
  *
  *     电话号和channel的映射
  *
  *
  * </p>
  *
  *
 　　* @author 郝江波
 　　* @date 2020/11/20 16:24
 　　*/
public class CallNumberAndChannelMapping {


    /**
     * 只有创建者或者加入房间的人 会被加入到这个map中
     */
    public static ConcurrentHashMap<String,  Channel> SESSION_CHANNEL_MAPPING = new ConcurrentHashMap<>();

    /**
     * 检查是否在线的mapping
     */
    public static ConcurrentHashMap<ChannelHandlerContext,  Long> CHECK_IS_ONLINE_MAPPING = new ConcurrentHashMap<>();

    /**
     * 房间号和mapping的映射
     */
    public static ConcurrentHashMap<Channel,  String> CHANNEL_SESSION_MAPPING = new ConcurrentHashMap<>();
    /**
     * 房间号和mapping的映射
     */
    public static ConcurrentHashMap<String,  Map<String,Channel>> ROOM_CHANNEL_MAPPING = new ConcurrentHashMap<>();
    /**
     * 房间号和udpCHannel的映射
     */
    public static ConcurrentHashMap<String, Map<String,InetSocketAddress>> UDP_ROOM_CHANNEL_MAPPING = new ConcurrentHashMap<>();
    /**
     * 房间映射
     */
    public static ConcurrentHashMap<String, RoomInfo> ROOM_MAPPING = new ConcurrentHashMap<>();


}
