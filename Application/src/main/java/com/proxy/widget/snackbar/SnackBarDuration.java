package com.proxy.widget.snackbar;

/**
 * SnackBar visibility duration before an animated slide out.
 */
@SuppressWarnings("unused")
public enum SnackBarDuration {
    LENGTH_SHORT(2000), LENGTH_LONG(3500);

    private final long duration;

    /**
     * Set snack duration.
     *
     * @param snackduration duration of visibility
     */
    SnackBarDuration(long snackduration) {
        this.duration = snackduration;
    }

    /**
     * Get Duration.
     *
     * @return the set duration
     */
    public long getDuration() {
        return duration;
    }
}
