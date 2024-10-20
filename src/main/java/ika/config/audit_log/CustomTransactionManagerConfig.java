package ika.config.audit_log;

import ika.utils.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.jpa.JpaTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.transaction.TransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

@Configuration
public class CustomTransactionManagerConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @Bean
    public JpaTransactionManager transactionManager() {
        return new CustomJpaTransactionManager(entityManagerFactory, dataSource, currentUserProvider);
    }

    public static class CustomJpaTransactionManager extends JpaTransactionManager {
        private final DataSource dataSource;
        private final CurrentUserProvider currentUserProvider;

        public CustomJpaTransactionManager(EntityManagerFactory emf, DataSource dataSource, CurrentUserProvider currentUserProvider) {
            super(emf);
            this.dataSource = dataSource;
            this.currentUserProvider = currentUserProvider;
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            super.doBegin(transaction, definition);
            try {
                Connection connection = DataSourceUtils.getConnection(dataSource);
                UUID userId = currentUserProvider.getCurrentUserId();
                String setUserQuery = "SET myapp.user_id = '" + userId + "'";
                String setAppNameQuery = "SET application_name = 'ika-api'";
                System.out.println("Setting user_id and application_name for the session.");
                connection.createStatement().execute(setUserQuery);
                connection.createStatement().execute(setAppNameQuery);
            } catch (SQLException e) {
                e.printStackTrace(); // Handle this properly for production environments
            }
        }

        @Override
        protected void doCleanupAfterCompletion(Object transaction) {
            super.doCleanupAfterCompletion(transaction);
            DataSourceUtils.releaseConnection(DataSourceUtils.getConnection(dataSource), dataSource);
        }
    }
}
