package model;

/*
 * ┌──────────────────────────────────────────────────────────────┐
 * │               <<interface>> Searchable                       │
 * ├──────────────────────────────────────────────────────────────┤
 * │ + matchesSearch(query: String): boolean                      │
 * │ + getSearchSummary(): String                                 │
 * ├──────────────────────────────────────────────────────────────┤
 * │ IMPLEMENTORS: User, Event (POLYMORPHISM)                     │
 * │ USED BY:      SearchPanel.performSearch                      │
 * └──────────────────────────────────────────────────────────────┘
 */
public interface Searchable {

    boolean matchesSearch(String query);

    String getSearchSummary();
}
