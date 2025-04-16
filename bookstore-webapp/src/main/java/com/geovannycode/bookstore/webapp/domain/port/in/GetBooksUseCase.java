package com.geovannycode.bookstore.webapp.domain.port.in;

import com.geovannycode.bookstore.webapp.domain.model.Book;

import java.util.List;

public interface GetBooksUseCase {
    List<Book> getAllBooks();
    Book getBookById(Long id);
}
