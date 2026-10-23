package net.hyze.core.shared.echo.packets;

import com.google.common.collect.Sets;
import dev.utils.echo.BufferIO;
import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.user.User;
import net.md_5.bungee.api.ChatColor;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SendTitlePacket extends AbstractEchoPacket {

    private String title = ChatColor.RESET.toString();
    private String subTitle = ChatColor.RESET.toString();
    private int fadeIn = 0;
    private int fadeOut = 1;
    private int stay = 0;

    protected Set<Integer> usersIds = Sets.newHashSet();
    protected Server server = null;

    public static SendTitlePacketBuilder builder() {
        return new SendTitlePacketBuilder();
    }

    @Override
    public void write(EchoByteBufferOutput buffer) {
        BufferIO.writeString(buffer, title);
        BufferIO.writeString(buffer, subTitle);
        buffer.writeInt(fadeIn);
        buffer.writeInt(fadeOut);
        buffer.writeInt(stay);

        buffer.writeInt(Optional.ofNullable(this.usersIds).map(Set::size).orElse(0));

        if (this.usersIds != null) {
            this.usersIds.forEach(buffer::writeInt);
        }

        BufferIO.writeEnum(buffer, server);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        title = buffer.readString();
        subTitle = buffer.readString();
        fadeIn = buffer.readInt();
        fadeOut = buffer.readInt();
        stay = buffer.readInt();

        this.usersIds = Sets.newHashSet();

        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            usersIds.add(buffer.readInt());
        }

        server = BufferIO.readEnum(buffer, Server.class);
    }

    public static class SendTitlePacketBuilder {

        private String title = ChatColor.RESET.toString();
        private String subTitle = ChatColor.RESET.toString();
        private int fadeIn = 0;
        private int fadeOut = 1;
        private int stay = 0;

        protected Set<Integer> usersIds = Sets.newHashSet();
        protected Server server = null;

        public SendTitlePacketBuilder title(String title) {
            this.title = title;
            return this;
        }

        public SendTitlePacketBuilder subTitle(String subTitle) {
            this.subTitle = subTitle;
            return this;
        }

        public SendTitlePacketBuilder fadeIn(int fadeIn) {
            this.fadeIn = fadeIn;
            return this;
        }

        public SendTitlePacketBuilder fadeOut(int fadeOut) {
            this.fadeOut = fadeOut;
            return this;
        }

        public SendTitlePacketBuilder stay(int stay) {
            this.stay = stay;
            return this;
        }

        public SendTitlePacketBuilder user(Set<User> users) {
            this.usersIds = users.stream().map(User::getId).collect(Collectors.toSet());
            return this;
        }

        public SendTitlePacketBuilder server(Server server) {
            this.server = server;
            return this;
        }

        public SendTitlePacket build() {
            return new SendTitlePacket(title, subTitle, fadeIn, fadeOut, stay, usersIds, server);
        }
    }
}
