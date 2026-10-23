package net.hyze.core.shared.storage.repositories;

import com.google.common.base.Throwables;
import dev.utils.shared.Printer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.contracts.repositories.Repository;
import net.hyze.core.shared.contracts.repositories.specs.Spec;
import net.hyze.core.shared.contracts.repositories.specs.SqlSpec;
import net.hyze.core.shared.providers.MysqlDatabaseProvider;
import net.hyze.core.shared.storage.repositories.specs.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@Getter
@RequiredArgsConstructor
public abstract class MysqlRepository implements Repository {

    @Getter
    protected final MysqlDatabaseProvider databaseProvider;


    public final <E, S extends Spec<E>> E query(S specification) {
        return this.query((SqlSpec<E>) specification);
    }

    protected <E> E query(SqlSpec<E> specification) {

        JdbcTemplate template = new JdbcTemplate(this.databaseProvider.provide().getDataSource());

        long start = System.currentTimeMillis();

        PreparedStatementCreator creator = specification.getPreparedStatementCreator();

        E out = null;

        Runnable callEnd = () -> Printer.INFO.coloredPrint(String.format(
                "[%s] Run spec '%s' in %sms [%s]",
                Thread.currentThread().getName(),
                specification.getClass().getSimpleName(),
                System.currentTimeMillis() - start,
                specification
        ));

        if (specification instanceof ExecuteSqlSpec) {
            ExecuteSqlSpec<E> executer = (ExecuteSqlSpec<E>) specification;

            out = template.execute(creator, executer.getPreparedStatementCallback());
        } else if (specification instanceof SelectSqlSpec) {
            SelectSqlSpec<E> selector = (SelectSqlSpec<E>) specification;

            out = template.query(creator, selector.getResultSetExtractor());
        } else if (specification instanceof UpdateSqlSpec) {
            UpdateSqlSpec<E> updater = (UpdateSqlSpec<E>) specification;

            int rowsAffected = template.update(creator);

            out = updater.parser(rowsAffected);
        } else if (specification instanceof DeleteSqlSpec) {
            DeleteSqlSpec<E> deleter = (DeleteSqlSpec<E>) specification;

            int rowsAffected = template.update(creator);

            out = deleter.parser(rowsAffected);
        } else if (specification instanceof InsertSqlSpec) {
            InsertSqlSpec<E> inserter = (InsertSqlSpec<E>) specification;

            KeyHolder holder = new GeneratedKeyHolder();

            int rowsAffected = template.update(creator, holder);

            out = inserter.parser(rowsAffected, holder);
        }

        if (System.getProperty("debug_specs") != null) {
            callEnd.run();

            if (System.getProperty("debug_specs_stack") != null) {
                Printer.INFO.coloredPrint(Throwables.getStackTraceAsString(new Throwable()), " ", " ");
            }
        }

        return out;
    }
}
