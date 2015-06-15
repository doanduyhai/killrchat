package com.datastax.demo.killrchat.service;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.demo.killrchat.exceptions.RememberMeDoesNotExistException;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.security.repository.CassandraRepository;
import info.archinnov.achilles.exception.AchillesBeanValidationException;
import info.archinnov.achilles.script.ScriptExecutor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.demo.killrchat.entity.UserEntity;
import info.archinnov.achilles.junit.AchillesResource;
import info.archinnov.achilles.junit.AchillesResourceBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Rule
    public AchillesResource resource = AchillesResourceBuilder
            .noEntityPackages(KEYSPACE)
            .withScript("cassandra/schema_creation.cql")
            .tablesToTruncate(USERS)
            .truncateBeforeAndAfterTest().build();
    @Rule
    public CassandraRepositoryRule rule = new CassandraRepositoryRule(resource);

    private Session session = resource.getNativeSession();
    private CassandraRepository repository = rule.getRepository();
    private ScriptExecutor scriptExecutor = resource.getScriptExecutor();

    private UserService service = new UserService();

    @Before
    public void setUp() {
        service.session = session;
        service.repository = repository;
    }

    @Test
    public void should_create_user() throws Exception {
        //Given
        final UserModel model = new UserModel("emc2", "a.einstein", "Albert", "EINSTEIN", "a.einstein@smart.com", "I am THE Genius");

        //When
        service.createUser(model);

        //Then
        final Row row = session.execute(select().from(USERS).where(eq("login", "emc2"))).one();

        assertThat(row).isNotNull();
        assertThat(row.getString("login")).isEqualTo("emc2");
        assertThat(row.getString("firstname")).isEqualTo("Albert");
        assertThat(row.getString("lastname")).isEqualTo("EINSTEIN");
        assertThat(row.getString("email")).isEqualTo("a.einstein@smart.com");
        assertThat(row.getString("bio")).isEqualTo("I am THE Genius");
    }


    @Test
    public void should_find_user_by_login() throws Exception {
        //Given
        scriptExecutor.executeScript("should_find_user_by_login.cql");

        //When
        final UserEntity foundUser = service.findByLogin("emc2");

        //Then
        assertThat(foundUser.getFirstname()).isEqualTo("Albert");
        assertThat(foundUser.getLastname()).isEqualTo("EINSTEIN");
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void should_fail_creating_user_if_already_exist() throws Exception {
        //Given
        final UserModel model = new UserModel("emc2", "a.einstein", "Albert", "EINSTEIN", "a.einstein@smart.com", "I am THE Genius");

        service.createUser(model);

        //When
        service.createUser(model);
    }

    @Test
    public void should_fetch_remember_me_user() throws Exception {
        //Given
        final Authentication authentication = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return "emc2";
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return "emc2";
            }
        };
        SecurityContextHolder.getContext().setAuthentication(authentication);
        scriptExecutor.executeScript("should_find_user_by_login.cql");

        //When
        final UserModel userModel = service.fetchRememberMeUser();

        //Then
        assertThat(userModel.getLogin()).isEqualTo("emc2");
        assertThat(userModel.getFirstname()).isEqualTo("Albert");
        assertThat(userModel.getLastname()).isEqualTo("EINSTEIN");
    }

    @Test(expected = RememberMeDoesNotExistException.class)
    public void should_throw_exception_when_trying_to_remember_me_expires() throws Exception {
        //Given
        final Authentication authentication = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return "anonymousUser";
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return "anonymousUser";
            }
        };
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //When
        service.fetchRememberMeUser();

        //Then

    }
}
