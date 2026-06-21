Message M_4D Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD J5.4 MESSAGE."
        Z. "END TRANSLATION."


}
CONDITION {
    1: "RECEIVED M.4D IS FOLLOWED BY AN M.84D MESSAGE AND RECEIVED M.4D/M.84D MESSAGE SEQUENCE IS LEGAL."
    2: "RECEIVED M.4D/M.84D IS FOLLOWED BY AN M.4B MESSAGE."
    3: "BEARING REPORT TYPE = 1."
    4: "M.84D(SW=0) BEARING INDICATOR = 1 OR M.84D(SW=1) BEARING INDICATOR = 1, OR NO M.84D(SW=1) RECEIVED."
}
IF (1) {
    IF (2) {
        IF (3) {
            IF (4) {
                EXECUTE(A, Z)


        }
        ELSE {
            EXECUTE(B, Z)


    }
}
ELSE {
    EXECUTE(B, Z)
}
}
ELSE {
    IF (3) {
        IF (4) {
            EXECUTE(A, Z)


    }
    ELSE {
        EXECUTE(B, Z)


}
}
ELSE {
    EXECUTE(B, Z)
}
}
}
ELSE {
    EXECUTE(A, Z)
}
}