package previous.A1;

public class Factory {
     
    public static Transport createTransport(String type) {
        if (type.equalsIgnoreCase("road")) {
            return new Train();
        } else if (type.equalsIgnoreCase("air")) {
            return new Airplane();
        } else {
            throw new IllegalArgumentException("Unknown transport type: " + type);
        }
    }

}
