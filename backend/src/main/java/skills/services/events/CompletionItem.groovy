package skills.services.events

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class CompletionItem {
    static enum CompletionItemType {
        Overall, Subject, Skill, Badge
    };
    CompletionItemType type
    Integer level // optional
    String id
    String name
    List<RecommendationItem> recommendations = []
}