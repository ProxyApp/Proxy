package com.shareyourproxy.util

/**
 * Public enums
 */
internal object Enumerations {
    /**
     * Edit Group Channel Activity types.
     */
    enum class GroupEditType {
        ADD_GROUP, EDIT_GROUP, PUBLIC_GROUP
    }

    /**
     * The type of view state a recycler view is in.
     */
    enum class ViewState {
        MAIN, EMPTY, LOADING
    }
}