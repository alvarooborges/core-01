package net.hyze.core.shared.user.storage.specs.hyze;

import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.storage.repositories.specs.UpdateSqlSpec;
import net.hyze.core.shared.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.PreparedStatementCreator;

@RequiredArgsConstructor
public class UpdateUserSpec extends UpdateSqlSpec<Void> {

    private final User user;

    private final int id;
    private final UUID uuid;
    private final String password;
    private String email;

    public UpdateUserSpec(User user) {
        this.user = user;

        this.id = user.getId();
        this.uuid = user.getUuid();
        this.password = user.getPassword();
        this.email = user.getEmail();
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return (Connection con) -> {
            PreparedStatement statement = con.prepareStatement(String.format(
                    "UPDATE `%s` SET `uuid` = ?, `password` = ?, `email` = ?, `email_verified_at` = ? WHERE `id` = ?;",
                    CoreConstants.Databases.Mysql.Tables.USERS_TABLE_NAME
            ));

            statement.setString(1, this.uuid == null ? null : this.uuid.toString());
            statement.setString(2, this.password);
            statement.setString(3, this.email);

            if (this.user.getEmailVerifiedAt() != null) {
                statement.setTimestamp(4, new Timestamp(this.user.getEmailVerifiedAt().getTime()));
            } else {
                statement.setNull(4, Types.TIMESTAMP);
            }

            statement.setInt(5, this.id);

            return statement;
        };
    }

    @Override
    public Void parser(int affectedRows) {
        return null;
    }
}
