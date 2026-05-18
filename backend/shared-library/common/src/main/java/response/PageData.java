package response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageData<T> {
    private List<T> items;
    private int totalPages;
    private int pageNumber;
    private int numberOfElements;

    public static <T> PageData<T> fromPageData(List<T> data, int totalPages, int current, int count) {
        return new PageData<T>(data, totalPages, current, count);
    }
}
