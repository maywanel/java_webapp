package com.example.demo.controller;

import com.example.demo.service.BookService;
import com.example.demo.model.Book;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchExternalBooks(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Query cannot be empty");
        }
        
        List<Book> results = bookService.searchBooksFromApi(query);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty() || 
            book.getAuthor() == null || book.getAuthor().trim().isEmpty() ||
            book.getDescription() == null || book.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid book data");
        }
        if (book.getTitle().length() > 255 || book.getAuthor().length() > 255 || 
            book.getDescription().length() > 2000) {
            return ResponseEntity.badRequest().body("Input exceeds maximum length");
        }
        if (bookService.getBookByName(book.getTitle().trim()) != null) {
            return ResponseEntity.status(409).body("Book with the same title already exists");
        }
        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        book.setDescription(book.getDescription().trim());
        Book savedBook = bookService.saveBook(book);
        return ResponseEntity.ok(savedBook);
    }

    @GetMapping
    public ResponseEntity<?> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty() || 
            book.getAuthor() == null || book.getAuthor().trim().isEmpty() ||
                book.getDescription() == null || book.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid book data");
        }
        if (book.getTitle().length() > 255 || book.getAuthor().length() > 255 || 
            book.getDescription().length() > 2000) {
            return ResponseEntity.badRequest().body("Input exceeds maximum length");
        }
        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        book.setDescription(book.getDescription().trim());
        Book updatedBook = bookService.updateBook(id, book);
        if (updatedBook == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Book updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        if (bookService.getBookById(id) == null) {
            return ResponseEntity.status(404).body("Book not found");
        }
        bookService.deleteBook(id);
        return ResponseEntity.ok("Book deleted successfully");
    }
}