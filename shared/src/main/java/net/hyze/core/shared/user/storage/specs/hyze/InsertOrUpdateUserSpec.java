package net.hyze.core.shared.user.storage.specs.hyze;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.storage.repositories.specs.InsertSqlSpec;
import net.hyze.core.shared.user.User;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
public class InsertOrUpdateUserSpec extends InsertSqlSpec<User> {

    private final String nick;
    private final UUID uuid;
    private final String password;
    private String email;
    private Date emailVerifiedAt;
    private Date createdAt;

    public InsertOrUpdateUserSpec(User user) {
        this.nick = user.getNick();
        this.uuid = user.getUuid();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.emailVerifiedAt = user.getEmailVerifiedAt();
        this.createdAt = user.getCreatedAt();
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return (Connection con) -> {
            PreparedStatement statement = con.prepareStatement(String.format(
                    "INSERT INTO `%s` (`nick`, `uuid`, `password`, `email`) VALUE (?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE `nick` = `nick`;",
                    CoreConstants.Databases.Mysql.Tables.USERS_TABLE_NAME
            ), Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, this.nick);
            statement.setString(2, this.uuid == null ? null : this.uuid.toString());
            statement.setString(3, this.password);
            statement.setString(4, this.email);

            return statement;
        };
    }

    @Override
    public User parser(int affectedRows, KeyHolder keyHolder) {
        if (affectedRows < 1) {
            return null;
        }

        return new User(keyHolder.getKey().intValue(), this.nick, this.uuid, this.password, this.email, this.emailVerifiedAt, this.createdAt);
    }
}
