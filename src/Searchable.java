/**
 * Aranabilir nesneler icin interface.
 * User ve Event bu interface'i implement eder -> polymorphism.
 */
public interface Searchable {

    boolean matchesSearch(String query);

    String getSearchSummary();
}
