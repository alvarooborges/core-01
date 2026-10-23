package net.hyze.core.spigot.misc.playerdata.storage;

import lombok.NonNull;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.providers.MysqlDatabaseProvider;
import net.hyze.core.shared.storage.repositories.MysqlRepository;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.misc.playerdata.storage.specs.InsertOrUpdateUserDataSpec;
import net.hyze.core.spigot.misc.utils.NBTTagCompoundUtils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class UserDataRepository extends MysqlRepository {

    public UserDataRepository(MysqlDatabaseProvider databaseProvider) {
        super(databaseProvider);
    }

    public boolean update(User user, @NonNull NBTTagCompound compound) {
        return query(new InsertOrUpdateUserDataSpec(user, compound));
    }

    public NBTTagCompound inspect(@NonNull User user) {
        String selectQuery = String.format(
                "SELECT `nbt` FROM `user_data` WHERE `user_id` = %s LIMIT 1;",
                user.getId()
        );

        ResultSet rs = null;

        try (Connection con = databaseProvider.provide().getDataSource().getConnection();
             Statement stm = con.createStatement()) {

            rs = stm.executeQuery(selectQuery);

            if (rs.next() && rs.getString("nbt") != null) {
                return NBTTagCompoundUtils.deserialize(rs.getString("nbt"));
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public NBTTagCompound fetch(@NonNull User user) {
        String updateQuery = String.format(
                "UPDATE `user_data` SET `app_authority` = '%s' WHERE `user_id` = %s LIMIT 1;",
                CoreProvider.getApp().getId(),
                user.getId()
        );

        String selectQuery = String.format(
                "SELECT `nbt` FROM `user_data` WHERE `user_id` = %s LIMIT 1;",
                user.getId()
        );

        ResultSet rs = null;

        try (Connection con = databaseProvider.provide().getDataSource().getConnection();
             Statement stm = con.createStatement()) {

            stm.execute(updateQuery);

            rs = stm.executeQuery(selectQuery);

            if (rs.next() && rs.getString("nbt") != null) {
                return NBTTagCompoundUtils.deserialize(rs.getString("nbt"));
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
