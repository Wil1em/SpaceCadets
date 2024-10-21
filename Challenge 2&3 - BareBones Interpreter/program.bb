# Clear variables m, n, p
clear m;
clear n;
clear p;

# Increment m and n
incr m;   # m = 1
incr n;   # n = 1

# Define subroutine to double increment p
sub doubleIncrementP;
    incr p;
    incr p;
end;

# Nested if-else inside while loop
while m not 0 do;
    decr m;  # m = 0
    if n not 0 do;
        call doubleIncrementP;  # p = 2
        decr n;                 # n = 0
    else;
        incr p;
    end;
end;

# Expected Output:
# m = 0, n = 0, p = 2
