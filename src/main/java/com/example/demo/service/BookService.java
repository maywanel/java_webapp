package com.example.demo.service;

import com.example.demo.dto.OpenLibraryResponse;
import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final WebClient webClient;

    public BookService(BookRepository bookRepository, WebClient.Builder webClientBuilder) {
        this.bookRepository = bookRepository;
        // Initialize WebClient with the Open Library base URL
        this.webClient = webClientBuilder.baseUrl("https://openlibrary.org").build();
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookByName(String title) {
        return bookRepository.findByTitle(title);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public Book updateBook(Long id, Book updatedBook) {
        return bookRepository.findById(id).map(book -> {
            book.setTitle(updatedBook.getTitle());
            book.setAuthor(updatedBook.getAuthor());
            book.setDescription(updatedBook.getDescription());
            return saveBook(book);
        }).orElse(null);
    }

    // --- NEW: API Search Method ---
    public List<Book> searchBooksFromApi(String query) {
        try {
            OpenLibraryResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/search.json")
                    .queryParam("q", query)
                    // .queryParam("title", query)
                    // .queryParam("author", query)
                    .queryParam("limit", 100)
                    .build())
                .retrieve()
                .bodyToMono(OpenLibraryResponse.class)
                .block();

            if (response == null || response.docs() == null) {
                return new ArrayList<>();
            }

            // Convert API response to your Book model
            return response.docs().stream().map(doc -> {
            Book book = new Book();
            book.setTitle(doc.title());

            // Authors
            if (doc.authorName() != null && !doc.authorName().isEmpty()) {
                book.setAuthor(String.join(", ", doc.authorName()));
            } else {
                book.setAuthor("Unknown Author");
            }
            
            // --- NEW COVER LOGIC ---
            if (doc.coverId() != null) {
                book.setCoverId(doc.coverId());
            }
            // -----------------------

            // (Optional) Keep ISBN logic if you want, but we rely on coverId now
            List<String> isbns = doc.isbn();
            if (isbns != null && !isbns.isEmpty()) {
                book.setIsbn(isbns.get(0));
            } else {
                book.setIsbn("N/A");
            }

            String desc = "First published in: " + 
                        (doc.firstPublishYear() != null ? doc.firstPublishYear() : "Unknown");
            book.setDescription(desc);            
            return book;
        }).collect(Collectors.toList());
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}