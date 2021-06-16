package generators;

import data.WECSplit;

import java.util.List;

public abstract class AGenerator<T> {
    public abstract List<T> generateTrainExamples(WECSplit queries, WECSplit passages);
    public abstract List<String> toPrintFormat(List<T> queryPassagePairs);
}
