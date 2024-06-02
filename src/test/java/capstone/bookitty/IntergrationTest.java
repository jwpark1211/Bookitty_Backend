package capstone.bookitty;

import capstone.bookitty.setup.BookStateSetup;
import capstone.bookitty.setup.CommentSetup;
import capstone.bookitty.setup.MemberSetup;
import capstone.bookitty.setup.StarSetup;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@Transactional
@Slf4j
@Disabled
@WithMockUser(roles = "USER")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BookittyApplication.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({
        RestDocsConfiguration.class,
        MemberSetup.class,
        BookStateSetup.class,
        StarSetup.class,
        CommentSetup.class
})
public class IntergrationTest {
    @Autowired protected MockMvc mvc;
    @Autowired protected MemberSetup memberSetup;
    @Autowired protected  StarSetup starSetup;
    @Autowired protected BookStateSetup stateSetup;
    @Autowired protected CommentSetup commentSetup;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected ResourceLoader resourceLoader;
    protected final String identifier = "{class-name}/{method-name}";
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
}
