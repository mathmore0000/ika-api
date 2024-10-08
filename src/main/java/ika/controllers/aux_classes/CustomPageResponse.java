package ika.controllers.aux_classes;

import ika.utils.GlobalValues;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class CustomPageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private Sort sort;
    private long offset;
    private int totalPages;

    // Construtor
    public CustomPageResponse(List<T> content, int pageNumber, int pageSize, Sort sort, long offset, int totalPages) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sort = sort;
        this.offset = offset;
        this.totalPages = totalPages;
    }

    public static int getValidSize(int size){
    // Ajustar o tamanho da página para estar dentro do limite
        System.out.println(GlobalValues.MIN_REQUEST.getValue());
        if (size < GlobalValues.MIN_REQUEST.getValue()) {
            size = GlobalValues.MIN_REQUEST.getValue();
        } else if (size > GlobalValues.MAX_REQUEST.getValue()) {
            size = GlobalValues.MAX_REQUEST.getValue();
        }
        return size;
    }
    public static int getValidPage(int page){
        if (page < 0) {
            page = 0;
        }
        return page;
    }



    // Getters e Setters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public static Pageable createPageableWithSort(int page, int size, String sortBy, String sortDirection) {
        // Definir a direção de ordenação com base no parâmetro recebido
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Criar a ordenação com base no campo e na direção fornecidos
        Sort sort = Sort.by(direction, sortBy);

        // Criar o Pageable com a ordenação configurada
        return PageRequest.of(page, size, sort);
    }
}
