package com.nixmash.web.db;

import com.nixmash.jangles.db.UsersDb;
import com.nixmash.jangles.db.UsersDbImpl;
import com.nixmash.jangles.db.cn.IConnection;
import com.nixmash.jangles.dto.Role;
import com.nixmash.jangles.dto.User;
import com.nixmash.jangles.service.UserService;
import com.nixmash.jangles.service.UserServiceImpl;
import com.nixmash.web.guice.TestConnection;
import io.bootique.BQRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

import static com.nixmash.jangles.utils.JanglesUtils.configureTestDb;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;

@RunWith(JUnit4.class)
public class UsersDbTest {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    private static UserService userService;

    @BeforeClass
    public static void setupDB() throws SQLException {
        BQRuntime runtime = TEST_FACTORY
                .app("--config=classpath:test.yml")
                .module(b -> b.bind(IConnection.class).to(TestConnection.class))
                .module(b -> b.bind(UserService.class).to(UserServiceImpl.class))
                .module(b -> b.bind(UsersDb.class).to(UsersDbImpl.class))
                .autoLoadModules()
                .createRuntime();

        userService = runtime.getInstance(UserService.class);

        try {
            configureTestDb("populate.sql");
        } catch (FileNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void newUserAddsTotalByOneTest() throws Exception {
        User jammer = new User("jammer", "jammer@aol.com", "Jammer", "McGee", "password");
        User saved = userService.createUser(jammer);

        User retrieved = userService.getUser("jammer");
        assertThat(saved.getUserId().intValue(), greaterThan(0));
        Assert.assertEquals(saved.getUserId(), retrieved.getUserId());
    }

    @Test
    public void addSecondUserTest() throws Exception {
        User reed = new User("reed", "reed@aol.com", "Reed", "Larson", "halo");
        User saved = userService.createUser(reed);

        User retrieved = userService.getUser("reed");
        Assert.assertEquals(saved.getUserId(), retrieved.getUserId());
    }

    @Test
    public void permissionsTest() throws Exception {
        List<Role> roles = userService.getRoles(1L);
        Assert.assertEquals(roles.size(), 2);
    }

}