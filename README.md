# Feylon

Feylon is a
[Concatenative](https://en.wikipedia.org/wiki/Concatenative_programming_language)
[Stack-Oriented](https://en.wikipedia.org/wiki/Stack-oriented_programming)
[Programming Language](https://en.wikipedia.org/wiki/Programming_language)
inspired by [Forth](https://en.wikipedia.org/wiki/Forth_(programming_language))
and [Porth](https://gitlab.com/tsoding/porth).

Feylon itself is currently written in Rust.  
Programs written in Feylon can be simulated in rust, or compiled as a native binary for 64-bit windows and linux.  
The old version written in Java can be found [here](https://github.com/ShiftingDawn/Feylon-Java).

## Examples

- Examples can be viewed [here](test/).

## Instructions

| Symbol | Pops | Pushes | Description                                                                                  | Example                        |
|--------|------|--------|----------------------------------------------------------------------------------------------|--------------------------------|
| dump   | 1    | 0      | Dumps the last item on the stack to STDOUT                                                   | `1 2 + dump` dumps 3 to STDOUT |
| dup    | 1    | 2      | Duplicates the last item on the stack                                                        | `1 dup` = 1, 1                 |
| drop   | 1    | 0      | Drops the last item on the stack, removing it                                                | `1 2 drop` = 1                 |
| over   | 2    | 4      | Duplicates the second-last item on the stack and places it at the end of the stack           | `1 2 over` = 1, 2, 1           |
| swap   | 2    | 2      | Swap the last two items on the stack                                                         | `1 2 swap` = 2, 1              |
| rot    | 3    | 3      | Pulls the third last item on the stack to the top                                            | `1 2 3 rot` = 2, 3, 1          |
| +      | 2    | 1      | Add two numbers and push the result onto the stack                                           | `1 2 +` = 3                    |
| -      | 2    | 1      | Subtracts two number and push the result onto the stack                                      | `3 1 -` = 2                    |
| *      | 2    | 1      | Multiply two numbers and push the result onto the stack                                      | `3 2 *` = 6                    |
| /      | 2    | 1      | Divide two numbers and push the result onto the stack                                        | `10 3 /` = 3                   |
| %      | 2    | 1      | Divide two numbers and push the remainder onto the stack                                     | `10 3 %` = 1                   |
| =      | 2    | 1      | Pushes 1 into the stack if the last two numbers are equal, 0 otherwise                       | `2 2 =` = 1, `2 3 =` = 0       |
| !=     | 2    | 1      | Pushes 1 into the stack if the last two numbers are not equal, 0 otherwise                   | `2 3 !=` = 1, `2 2 !=` = 2     |
| <      | 2    | 1      | Pushes 1 into the stack if the first number is smaller than the second, 0 otherwise          | `2 3 <` = 1, `3 2 <` = 0       |
| \>     | 2    | 1      | Pushes 1 into the stack if the first number is larger than the second, 0 otherwise           | `3 2 >` = 1, `2 2 >` = 0       |
| <=     | 2    | 1      | Pushes 1 into the stack if the first number is smaller or equal than the second, 0 otherwise | `2 3 <=` = 1, `2 3 <=` = 1     |
| \>=    | 2    | 1      | Pushes 1 into the stack if the first number is larger or equal  than the second, 0 otherwise | `3 2 >=` = 1, `2 2 >=` = 1     |
| \<<    | 2    | 1      | Shifts the last item on the stack left by the item before that                               | `2 2 <<` = 8                   |
| \>>    | 2    | 1      | Shifts the last item on the stack right by the item before that                              | `8 1 >>` = 4                   |
| \&     | 2    | 1      | Performs a bitwise AND on the last two items on the stack                                    | `13 37 &` = 5                  |
| \|     | 2    | 1      | Performs a bitwise OR on the last two items on the stack                                     | `13 37 \|` = 45                |
| \^     | 2    | 1      | Performs a bitwise XOR on the last two items on the stack                                    | `13 37 ^` = 40                 |

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

**Allocating memory**

```forth
memory <mem_name> <size> end
```

**Storing data into memory**

```forth
<value> <mem_name> store   //Store a byte (lowest 8 bits) in <mem_name>
<value> <mem_name> store16 //Store a short (lowest 2 bytes) in <mem_name>
<value> <mem_name> store32 //Store an integer (4 bytes) in <mem_name>
<value> <mem_name> store32 //Store a long (8 bytes) in <mem_name>
```

**Loading data from memory**

```forth
<mem_name> load   //Places a byte from <mem_name> on the stack
<mem_name> load16 //Places a short number from <mem_name> on the stack
<mem_name> load32 //Places an int from <mem_name> on the stack
<mem_name> load64 //Places a long from <mem_name> on the stack
```

## Functions

Code can be reused by wrapping it in a function.

```forth
function max(int int -> int)
  over over < if swap end
  pop
end

1 2 max
dump // 2
```

## Variables

Working with the stack can sometimes result in code looking like a jungle of keywords like `dup`, `over`, `swap` and `rot`.
To combat this, data on the stack can be bound to variables.

```forth
//Stack: [1, 2, 3, 4, 5]
var (a b c d e)
  a dump // 1
  b dump // 2
  c dump // 3
  d dump // 4
  e dump // 5
  a dump // 1
  a dump // 1
end
```

Variables will be consumed when binding, but can be used multiple times without the need to duplicate them. If you want to retain the variables on the stack, you can simply call
them without consuming
them before the end of the block.
Variables consume N items from the end of the stack, with N being the number of variable names.

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
