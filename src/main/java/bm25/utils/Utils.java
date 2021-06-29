package bm25.utils;

import bm25.data.Mention;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {
    public static boolean assertNoDups(List<Mention> listToCheck) {
        Set<String> queryIds = listToCheck.stream().map(Mention::getMention_id).collect(Collectors.toSet());
        return queryIds.size() == listToCheck.size();
    }
}
