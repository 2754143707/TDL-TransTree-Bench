Message J10_6 Translation Trees {
    ACTION {
        A."DISCARD MESSAGE."
        B."FWD M.9B."
        Z."END TRANSLATION."


}
CONDITION {
    1:"PAIRING ACTION = 0-7 OR 15."
}
IF (1) {
    EXECUTE(B,Z)
}
ELSE {
    EXECUTE(A,Z)
}
}
