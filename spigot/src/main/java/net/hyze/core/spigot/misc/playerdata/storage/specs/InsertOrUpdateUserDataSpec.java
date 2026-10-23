package net.hyze.core.spigot.misc.playerdata.storage.specs;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.storage.repositories.specs.UpdateSqlSpec;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CoreSpigotConstants;
import net.hyze.core.spigot.misc.utils.NBTTagCompoundUtils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

@RequiredArgsConstructor
public class InsertOrUpdateUserDataSpec extends UpdateSqlSpec<Boolean> {

    private final User user;
    private final NBTTagCompound compound;

    @Override
    public Boolean parser(int affectedRows) {
        return affectedRows > 0;
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return (Connection con) -> {
            PreparedStatement statement = con.prepareStatement(String.format(
                    "INSERT INTO `%s` (`user_id`, `nbt`) VALUES (?, ?) "
                            + "ON DUPLICATE KEY UPDATE `nbt` = IF(`app_authority` IS NULL OR `app_authority` = ?, VALUES(`nbt`), `nbt`);",
                    CoreSpigotConstants.Databases.Mysql.Tables.USER_DATA_TABLE_NAME
            ), Statement.RETURN_GENERATED_KEYS);

            statement.setInt(1, user.getId());
            statement.setString(2, NBTTagCompoundUtils.serialize(compound));
            statement.setString(3, CoreProvider.getApp().getId());

            return statement;
        };
    }
}
