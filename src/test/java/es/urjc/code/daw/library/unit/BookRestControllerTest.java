package es.urjc.code.daw.library.unit;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class BookRestControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @Captor
    private ArgumentCaptor<Book> bookArgumentCaptor;

    @Test
    @WithAnonymousUser
    public void whenRequestAllBookAsAnonymousUserThenReturnAllBooks() throws Exception {
        List<Book> bookList = Arrays.asList(
                new Book("UML", "The Unified Modeling Language User Guide"),
                new Book("Refactoring", "Improving the Design of Existing Code"));
        when(bookService.findAll()).thenReturn(bookList);

        mvc.perform(get("/api/books/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", equalTo("UML")))
                .andExpect(jsonPath("$[0].description", equalTo("The Unified Modeling Language User Guide")))
                .andExpect(jsonPath("$[1].title", equalTo("Refactoring")))
                .andExpect(jsonPath("$[1].description", equalTo("Improving the Design of Existing Code")));
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    public void whenRequestAllBookAsLoggedUserThenReturnAllBooks() throws Exception {
        List<Book> bookList = Arrays.asList(
                new Book("UML", "The Unified Modeling Language User Guide"),
                new Book("Refactoring", "Improving the Design of Existing Code"));
        when(bookService.findAll()).thenReturn(bookList);

        mvc.perform(get("/api/books/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", equalTo("UML")))
                .andExpect(jsonPath("$[0].description", equalTo("The Unified Modeling Language User Guide")))
                .andExpect(jsonPath("$[1].title", equalTo("Refactoring")))
                .andExpect(jsonPath("$[1].description", equalTo("Improving the Design of Existing Code")));    }

    @Test
    @WithAnonymousUser
    public void whenRequestAddBookAsAnonymousUserThenReturnError() throws Exception {
        mvc.perform(post("/api/books/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"title\": \"Test title\", \"description\":\"test description\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    public void whenRequestAddBookAsLoggedUserThenReturnSuccess() throws Exception {
        mvc.perform(post("/api/books/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"title\": \"Test title\", \"description\":\"test description\"}"))
                .andExpect(status().isCreated());

        verify(bookService).save(bookArgumentCaptor.capture());

        assertEquals("Test title", bookArgumentCaptor.getValue().getTitle());
        assertEquals("test description", bookArgumentCaptor.getValue().getDescription());
    }

    @Test
    @WithAnonymousUser
    public void whenRequestDeleteBookAsAnonymousUserTheReturnError() throws Exception {
        mvc.perform(delete("/api/books/54")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    public void whenRequestDeleteBookAsLoggedUserThenReturnError() throws Exception {
        mvc.perform(delete("/api/books/54")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="admin",roles={"USER", "ADMIN"})
    public void whenRequestDeleteBookAsAdminThenReturnSuccess() throws Exception {
        mvc.perform(delete("/api/books/54")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookService).delete(eq(54L));
    }
}
