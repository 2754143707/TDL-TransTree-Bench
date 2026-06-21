Message Unknown Translation Trees {
    ACTION {
        G. "Unknown Action"
        K. "Unknown Action"
        F. "Unknown Action"
        H. "Unknown Action"
        I. "Unknown Action"
        J. "Unknown Action"
        Z. "Unknown Action"


}
CONDITION {
    4: "Unknown Condition"
    5: "Unknown Condition"
    6: "Unknown Condition"
    7: "Unknown Condition"
    8: "Unknown Condition"
}
IF (4) {
    EXECUTE (G,K)
}
ELSE {
    IF (5) {
        IF (6) {
            IF (7) {
                IF (8) {
                    EXECUTE (F,H,I,J,Z)


            }
            ELSE {
                EXECUTE (F,H,I,J,Z)


        }


}
ELSE {
    IF (8) {
        EXECUTE (F,H,I,J,Z)


}
ELSE {
    EXECUTE (I,J,Z)
}
}
}
ELSE {
    IF (8) {
        EXECUTE (F,H,J,Z)


}
ELSE {
    EXECUTE (I,J,Z)
}
}
}
ELSE {
    IF (6) {
        IF (7) {
            EXECUTE (F,H,J,Z)


    }
    ELSE {
        EXECUTE (I,J,Z)


}
}
ELSE {
    EXECUTE (Z)
}
}
}
}