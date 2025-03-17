# Feylon

Feylon is a
[Concatenative](https://en.wikipedia.org/wiki/Concatenative_programming_language)
[Stack-Oriented](https://en.wikipedia.org/wiki/Stack-oriented_programming)
[Programming Language](https://en.wikipedia.org/wiki/Programming_language)
inspired by [Forth](https://en.wikipedia.org/wiki/Forth_(programming_language))
and [Porth](https://gitlab.com/tsoding/porth).

Feylon is currently interpreted in Java, but may be compiled to a native binary in the future.

## Examples

- Examples can be viewed [here](src/test/feylon).
- Tests for the language itself can be viewed [here](src/test/java/com/shiftingdawn/feylon/tests)

## Instructions

| Symbol   | Pops | Pushes | Description                                                                                  | Example                          |
|----------|------|--------|----------------------------------------------------------------------------------------------|----------------------------------|
| dup      | 1    | 2      | Duplicates the last item on the stack                                                        | `1 dup` = 1, 1                   |
| dup2     | 2    | 4      | Duplicates the last two items on the stack                                                   | `1 2 dup2` = 1, 2, 1, 2          |
| pop      | 1    | 0      | Pops the last item on the stack, effectively removing it                                     | `1 2 pop` = 1                    |
| swap     | 2    | 2      | Swap the last two items on the stack                                                         | `1 2 swap` = 2, 1                |
| swap2    | 3    | 3      | Pulls the third last item on the stack to the top                                            | `1 2 3 swap` = 2, 3, 1           |
| +        | 2    | 1      | Add two numbers and push the result onto the stack                                           | `1 2 +` = 3                      |
| -        | 2    | 1      | Subtracts two number and push the result onto the stack                                      | `3 1 -` = 2                      |
| *        | 2    | 1      | Multiply two numbers and push the result onto the stack                                      | `3 2 *` = 6                      |
| /        | 2    | 1      | Divide two numbers and push the result onto the stack                                        | `10 3 /` = 3                     |
| %        | 2    | 1      | Divide two numbers and push the remainder onto the stack                                     | `10 3 %` = 1                     |
| =        | 2    | 1      | Pushes 1 into the stack if the last two numbers are equal, 0 otherwise                       | `2 2 =` = 1, `2 3 =` = 0         |
| !=       | 2    | 1      | Pushes 1 into the stack if the last two numbers are not equal, 0 otherwise                   | `2 3 !=` = 1, `2 2 !=` = 2       |
| <        | 2    | 1      | Pushes 1 into the stack if the first number is smaller than the second, 0 otherwise          | `2 3 <` = 1, `3 2 <` = 0         |
| \>       | 2    | 1      | Pushes 1 into the stack if the first number is larger than the second, 0 otherwise           | `3 2 >` = 1, `2 2 >` = 0         |
| <=       | 2    | 1      | Pushes 1 into the stack if the first number is smaller or equal than the second, 0 otherwise | `2 3 <=` = 1, `2 3 <=` = 1       |
| \>=      | 2    | 1      | Pushes 1 into the stack if the first number is larger or equal  than the second, 0 otherwise | `3 2 >=` = 1, `2 2 >=` = 1       |
| \<<      | 2    | 1      | Shifts the last item on the stack left by the item before that                               | `2 2 <<` = 8                     |
| \>>      | 2    | 1      | Shifts the last item on the stack right by the item before that                              | `8 1 >>` = 4                     |
| \&       | 2    | 1      | Performs a bitwise AND on the last two items on the stack                                    | `13 37 &` = 5                    |
| \|       | 2    | 1      | Performs a bitwise OR on the last two items on the stack                                     | `13 37 \|` = 45                  |
| \^       | 2    | 1      | Performs a bitwise XOR on the last two items on the stack                                    | `13 37 ^` = 40                   |
| mem      | 0    | 1      | Pushes a pointer to the start of shared memory onto the stack                                | `mem` = 0xff                     |
| memset   | 2    | 0      | Pushes the last value on the stack into the pointer before that                              | `mem 10 memset` = \[...,10,...\] |
| memget   | 1    | 1      | Pushes a value from memory at a popped pointer onto the stack                                | `mem memget` = 10                |
| dump     | 1    | 0      | Dumps the last item on the stack to STDOUT                                                   | `1 2 + dump` dumps 3 to STDOUT   |
| syscall3 | 3    | 0      | See the [SysCall](#SysCall) section                                                          | `x y z syscall3`                 |

## The stack

The stack can be thought of as a LIFO queue, meaning data is always added to the end last and removed from the end first.
It is not possible to push or pop into other regions of the stack.

### Integers

Integers are defined by writing any (whole) number.

```forth
//Stack before: [ ]
1 2 3
//Stack after: [ 1, 2, 3 ]
-10 -4
//Stack after: [ 1, 2, 3, -10, -4 ]
```

### Strings

String are defined by putting text between double quotes `"`.
Strings are stored in a special place in memory.
When defining a string, the length of the string and a pointer to the memory address are pushed onto the stack.

```forth
//Stack before: [ ]
"hello"
//Stack after: [ 5, *ptr ]
"this is a string with spaces"
//Stack after: [ 5, *ptr, 28, *ptr ]
```

## Memory

**Storing data into memory**

```forth
mem 10 memset       // Stack: [ *ptr ]             Memory: [ 10 ]
mem 1 + 20 memset   // Stack: [ *ptr, *ptr ]        Memory: [ 10, 20 ]
mem 2 + 30 memset   // Stack: [ *ptr, *ptr, *ptr ]   Memory: [ 10, 20, 30 ]
```

**Loading data from memory**

```forth
mem memget          // Stack: [ 10 ]         Memory: [ 10, 20, 30 ]
mem 1 + memget      // Stack: [ 10, 20 ]     Memory: [ 10, 20, 30 ]
mem 2 + memget      // Stack: [ 10, 20, 30 ] Memory: [ 10, 20, 30 ]
```

## Functions

Code can be reused by wrapping it in a function.

**Get the bigger of two numbers:**

```forth
function max
  dup2 < if swap end
  pop
end

1 2 max
```

**Print a string to stderr:**

```forth
function printE
  2 1 syscall3
end

"This is an error" printE
```

## Imports

Files can import other files, virtually merging the two files.
Imports are defined by declaring a path to the file to import as a string.
The file path is relative to the file the `import` was made from, NOT the current working directory.

All imported content will be available right after the `import` call, but not before it.

```forth
//Valid:
import "math.fey"
1 2 max
print

//Not valid:
1 2 max
import "math.fey"
print
```

## SysCall

It is possible to execute [syscalls](https://chromium.googlesource.com/chromiumos/docs/+/master/constants/syscalls.md#x86_64-64_bit) in Feylon.
The syscall function to use depends on the number of arguments of the syscall.
Arguments should be pushed to the stack in reverse order.

| Symbol   | Arguments | Usage                                                               |
|----------|-----------|---------------------------------------------------------------------|
| syscall0 | 0         | \<syscall> syscall0                                                 |
| syscall1 | 1         | \<arg1> \<syscall> syscall1                                         |
| syscall2 | 2         | \<arg2> \<arg1> \<syscall> syscall2                                 |
| syscall3 | 3         | \<arg3> \<arg2> \<arg1> \<syscall> syscall3                         |
| syscall4 | 4         | \<arg4> \<arg3> \<arg2> \<arg1> \<syscall> syscall4                 |
| syscall5 | 5         | \<arg5> \<arg4> \<arg3> \<arg2> \<arg1> \<syscall> syscall5         |
| syscall6 | 6         | \<arg6> \<arg5> \<arg4> \<arg3> \<arg2> \<arg1> \<syscall> syscall6 |