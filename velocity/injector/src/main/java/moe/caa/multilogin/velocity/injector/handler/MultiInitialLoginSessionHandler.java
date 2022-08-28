package moe.caa.multilogin.velocity.injector.handler;

import com.google.common.primitives.Longs;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.AuthSessionHandler;
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import com.velocitypowered.proxy.crypto.EncryptionUtils;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import lombok.Getter;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import net.kyori.adventure.text.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 接管 InitialLoginSessionHandler 类的其中一个方法
 */
@Getter()
public class MultiInitialLoginSessionHandler {

    // LoginStateEnum 的枚举
    private static Enum<?> loginStateEnum$LOGIN_PACKET_EXPECTED;
    private static Enum<?> loginStateEnum$LOGIN_PACKET_RECEIVED;
    private static Enum<?> loginStateEnum$ENCRYPTION_REQUEST_SENT;
    private static Enum<?> loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED;

    // 一些函数和字段的引用
    private static MethodHandle assertStateMethod;
    private static MethodHandle setCurrentStateField;
    private static MethodHandle getLoginField;
    private static MethodHandle getVerifyField;
    private static MethodHandle getServerField;
    private static MethodHandle getInboundField;
    private static MethodHandle getMcConnectionField;
    private static MethodHandle getCurrentStateField;
    private static MethodHandle authSessionHandler_allArgsConstructor;
    // 类体常量
    private final InitialLoginSessionHandler initialLoginSessionHandler;
    private final MultiCoreAPI multiCoreAPI; // 这个不是
    private final VelocityServer server;
    private final MinecraftConnection mcConnection;
    private final LoginInboundConnection inbound;
    // 运行时改动的实例
    private ServerLogin login;
    private byte[] verify;
    // 自己的对象，表示是否通过加密
    private boolean encrypted = false;

    public MultiInitialLoginSessionHandler(InitialLoginSessionHandler initialLoginSessionHandler, MultiCoreAPI multiCoreAPI) {
        this.initialLoginSessionHandler = initialLoginSessionHandler;
        this.multiCoreAPI = multiCoreAPI;
        try {
            this.server = (VelocityServer) getServerField.invoke(initialLoginSessionHandler);
            this.mcConnection = (MinecraftConnection) getMcConnectionField.invoke(initialLoginSessionHandler);
            this.inbound = (LoginInboundConnection) getInboundField.invoke(initialLoginSessionHandler);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        Class<InitialLoginSessionHandler> initialLoginSessionHandlerClass = InitialLoginSessionHandler.class;
        Class<?> loginStateEnum = Class.forName("com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler$LoginState");

        // 获取枚举常量
        for (Object constant : loginStateEnum.getEnumConstants()) {
            final Enum<?> enumObject = (Enum<?>) constant;
            switch ((enumObject).name()) {
                case "LOGIN_PACKET_EXPECTED":
                    loginStateEnum$LOGIN_PACKET_EXPECTED = enumObject;
                    break;
                case "LOGIN_PACKET_RECEIVED":
                    loginStateEnum$LOGIN_PACKET_RECEIVED = enumObject;
                    break;
                case "ENCRYPTION_REQUEST_SENT":
                    loginStateEnum$ENCRYPTION_REQUEST_SENT = enumObject;
                    break;
                case "ENCRYPTION_RESPONSE_RECEIVED":
                    loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED = enumObject;
                    break;
            }
        }
        Objects.requireNonNull(loginStateEnum$LOGIN_PACKET_EXPECTED, "LOGIN_PACKET_EXPECTED");
        Objects.requireNonNull(loginStateEnum$LOGIN_PACKET_RECEIVED, "LOGIN_PACKET_RECEIVED");
        Objects.requireNonNull(loginStateEnum$ENCRYPTION_REQUEST_SENT, "ENCRYPTION_REQUEST_SENT");
        Objects.requireNonNull(loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED, "ENCRYPTION_RESPONSE_RECEIVED");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        assertStateMethod = lookup.unreflect(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredMethod("assertState", loginStateEnum)
        ));

        setCurrentStateField = lookup.unreflectSetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("currentState")
        ));

        getLoginField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("login")
        ));

        getVerifyField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("verify")
        ));

        getServerField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("server")
        ));

        getInboundField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("inbound")
        ));

        getMcConnectionField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("mcConnection")
        ));

        getCurrentStateField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("currentState")
        ));

        authSessionHandler_allArgsConstructor = lookup.unreflectConstructor(ReflectUtil.handleAccessible(
                AuthSessionHandler.class.getDeclaredConstructor(
                        VelocityServer.class,
                        LoginInboundConnection.class,
                        com.velocitypowered.api.util.GameProfile.class,
                        boolean.class
                )
        ));
    }

    private void initValues() throws Throwable {
        this.login = (ServerLogin) getLoginField.invoke(initialLoginSessionHandler);
        this.verify = (byte[]) getVerifyField.invoke(initialLoginSessionHandler);
    }

    public void handle(EncryptionResponse packet) throws Throwable {
        initValues();

        // 模拟常规流程
        assertStateMethod.invoke(initialLoginSessionHandler, loginStateEnum$ENCRYPTION_REQUEST_SENT);
        setCurrentStateField.invoke(initialLoginSessionHandler, loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED);

        ServerLogin login = this.login;
        if (login == null) {
            throw new IllegalStateException("No ServerLogin packet received yet.");
        }
        if (this.verify.length == 0) {
            throw new IllegalStateException("No EncryptionRequest packet sent yet.");
        }

        try {

            // 加密部分
            KeyPair serverKeyPair = this.server.getServerKeyPair();
            if (this.inbound.getIdentifiedKey() != null) {
                IdentifiedKey playerKey = this.inbound.getIdentifiedKey();
                if (!playerKey.verifyDataSignature(packet.getVerifyToken(), this.verify, Longs.toByteArray(packet.getSalt()))) {
                    throw new IllegalStateException("Invalid client public signature.");
                }
            } else {
                byte[] decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, packet.getVerifyToken());
                if (!MessageDigest.isEqual(this.verify, decryptedSharedSecret)) {
                    throw new IllegalStateException("Unable to successfully decrypt the verification token.");
                }
            }

            byte[] decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, packet.getSharedSecret());

            encrypted = true;
            // 验证
            String username = login.getUsername();
            String serverId = EncryptionUtils.generateServerId(decryptedSharedSecret, serverKeyPair.getPublic());
            String playerIp = ((InetSocketAddress) this.mcConnection.getRemoteAddress()).getHostString();

            AuthResult result = multiCoreAPI.getAuthHandler().auth(username, serverId, playerIp);

            if (this.mcConnection.isClosed()) return;
            try {
                this.mcConnection.enableEncryption(decryptedSharedSecret);
            } catch (GeneralSecurityException var8) {
                LoggerProvider.getLogger().error("Unable to enable encryption for connection", var8);
                this.mcConnection.close(true);
                return;
            }
            if (result.isAllowed()) {
                this.mcConnection.setSessionHandler(
                        (AuthSessionHandler) authSessionHandler_allArgsConstructor.invoke(
                                this.server, inbound, generateGameProfile(result.getResponse()), true
                        )
                );
            } else {
                this.inbound.disconnect(Component.text(result.getKickMessage()));
            }
        } catch (GeneralSecurityException var9) {
            LoggerProvider.getLogger().error("Unable to enable encryption.", var9);
            this.mcConnection.close(true);
        }
    }

    private com.velocitypowered.api.util.GameProfile generateGameProfile(GameProfile response) {
        return new com.velocitypowered.api.util.GameProfile(
                response.getId(),
                response.getName(),
                response.getPropertyMap().values().stream().map(s ->
                        new com.velocitypowered.api.util.GameProfile.Property(s.getName(), s.getValue(), s.getSignature())
                ).collect(Collectors.toList())
        );
    }
}
