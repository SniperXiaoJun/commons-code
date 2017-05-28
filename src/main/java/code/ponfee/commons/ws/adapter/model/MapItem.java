package code.ponfee.commons.ws.adapter.model;

/**
 * 不能加泛型：ParameterizedTypeImpl cannot be cast to TypeVariable
 * @author fupf
 */
@SuppressWarnings("rawtypes")
public class MapItem {
    private MapEntry[] item;

    public MapItem() {}

    public MapItem(MapEntry[] item) {
        this.item = item;
    }

    public MapEntry[] getItem() {
        return item;
    }

    public void setItem(MapEntry[] item) {
        this.item = item;
    }
}
