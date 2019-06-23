export class ArrayUtils {
    static first<T>(values: T[]): T {
        if (values == null || values.length == 0) return null;
        return values[0];
    }
}