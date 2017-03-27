package io.tchepannou.kiosk.pipeline.step.validation;

public class Validation {
    private static final Validation SUCCESS = new Validation(true, null);

    private final boolean success;
    private final String reason;

    private Validation(final boolean success, final String reason) {
        this.success = success;
        this.reason = reason;
    }

    public static Validation success(){
        return SUCCESS;
    }
    public static Validation failure(final String reason){
        return new Validation(false, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Validation))
            return false;

        final Validation that = (Validation) o;

        if (isSuccess() != that.isSuccess())
            return false;
        return getReason().equals(that.getReason());

    }

    @Override
    public int hashCode() {
        int result = (isSuccess() ? 1 : 0);
        result = 31 * result + getReason().hashCode();
        return result;
    }
}
