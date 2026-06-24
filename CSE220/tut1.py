
single_quoted = 'Hello World'
double_quoted = "Hello World"

multiline_string = """This string spans across
multiple lines seamlessly,
preserving the formatting."""

print (single_quoted)
print (multiline_string)




# This is a standard single-line comment

"""
This behaves as a multi-line comment.
Because it is not assigned to any identifier,
the Python compiler discards it.
"""



# Halts execution until Enter is pressed
input("\n\nPress the enter key to exit.")

# Reading a raw string
username = input("Enter username: ")

# Reading an integer (requires explicit type casting)
age = int(input("Enter your age: "))



#forced multiline
import sys; x = 100; print(x)



# Suppressing the default newline by passing an empty string
print("Hello ", end='')
print("World!")  # Outputs: "Hello World!" on a single line


#multi assignment
x=y=z=1
a,b,c= 10, 45 ,'sami'



#type changes as the object changes

x = 42
print(type(x))  # Output: <class 'int'>

x = "Hello"
print(type(x))  # Output: <class 'str'>





large_int = 9999999999999999999999999999999999999  # No overflow!
real_pi = 3.1415926535

complex_num = 3 + 4j
print(complex_num.real)  # Output: 3.0
print(complex_num.imag)  # Output: 4.0



my_str = "Python"
# my_str[0] = 'J' <-- Throws a TypeError: 'str' object does not support item assignment



# A single list storing integers, strings, and individual characters
my_list = [1, 2, "String", 'a']
print(my_list)  # Output: String
my_list[1] = 4 #can be updated
print(my_list)


my_tuple = (1, 2, "String", 'a')
# my_tuple[1] = 99 <-- Throws a TypeError: 'tuple' object does not support item assignment







# --- Setup Context ---
letters = ['a', 'b', 'c', 'd', 'e']

# --- Slicing Elements ---
sub_list = letters[1:4]  # Starts at index 1 ('b'), stops before index 4 ('e')
print(sub_list)          # Output: ['b', 'c', 'd']

last_item = letters[-3:-1]  # Evaluates to c. d 
print(last_item)     # remember -1 . -3 will not work; have to go from left to right & negative indices from right to left

# --- Concatenation ---
combined = [1, 2] + [3, 4]
print(combined)          # Output: [1, 2, 3, 4]

# --- Repetition ---
repeated = ["Hi"] * 3
print(repeated)          # Output: ['Hi', 'Hi', 'Hi']

# --- Length Evaluation ---
print(len(letters))      # Output: 5






# Initializing a map tracking employee data
employee = {
    "name": "John Doe",
    "id": 5543,
    "dept": "Engineering"
}

# Accessing values via square brackets
print(employee["name"])  # Output: John Doe

# Appending a new key-value link dynamically
employee["salary"] = 80000






print(9 / 2)   # Standard division evaluates to Float: 4.5
print(9 // 2)  # Floor division truncates to Integer: 4
print(9.0 // 2.0)  # Evaluates to Float representation: 4.0
print (9**2) #exponent
print(bin(10))  # Output: '0b1010'




nums = [1, 2, 3, 4, 5]
print(3 in nums)      # Output: True
print(10 not in nums)  # Output: True




list_a = [1, 2, 3]
list_b = list_a  # Copying the memory address reference
list_c = [1, 2, 3]  # Instantiating a separate list object with identical data values

print(list_a == list_c)  # True (Their values are identical)
print(list_a is list_c)  # False (They are completely different objects in memory)
print(list_a is list_b)  # True (They point to the exact same underlying heap object)






score = 85

if score >= 90:
    print("Grade: A")
elif score >= 80:
    print("Grade: B")
else:
    print("Grade: C")





count = 0
while count < 3:
    print(count)
    count += 1



# Iterates from 0 up to 4 (5 is excluded)
for i in range(0, 5):
    print(i) # Outputs: 0, 1, 2, 3, 4



# Constructing a tangible physical list from an iterator range
numbers_list = list(range(1, 6))
print(numbers_list)  # Output: [1, 2, 3, 4, 5]

for item in range(1, 4):
    print(item)
else:
    print("Loop finished normally!") # This executes

print("---")

for item in range(1, 4):
    if item == 2:
        break # Aborting loop execution prematurely
    print(item)
else:
    print("Loop finished normally!") # This does NOT execute because of the break statement







def function_name(parameter_list):
    """Optional Docstring for Documentation purposes"""
    # Function execution suite
    return value

# Docstrings: If the first line inside a function is a string literal enclosed in triple quotes,
#  Python treats it as a structural Docstring. This string is parsed into the function object's __doc__ metadata 
# attribute and is used by IDEs to generate tooltips.
# Implicit Returns: If a function does not contain an explicit return statement, 
# r calls a blank return with no argument, it implicitly returns a standard None object.





# --- Definition Layout ---
def configure_profile(username, status="Active", *hobbies):
    print(f"User: {username}, Status: {status}")
    print(f"Hobbies recorded: {hobbies}")

# --- 1. Calling via standard Positional (Required) Arguments ---
configure_profile("Sami", "Active") 

# --- 2. Calling via Explicit Keyword Arguments (Altering Order) ---
configure_profile(status="Suspended", username="Alex")

# --- 3. Utilizing Default Argument Mechanics ---
configure_profile("John") # status automatically defaults to "Active"

# --- 4. Processing Variable-length Arguments ---
configure_profile("Jane", "Active", "Coding", "Gaming", "Music")
# "Coding", "Gaming", and "Music" are packed cleanly into the hobbies tuple






def calculate_metrics(length, width):
    area = length * width
    perimeter = 2 * (length + width)
    return area, perimeter  # Seamlessly packs values into an implicit tuple

# Unpacking the tuple directly on return execution
room_area, room_perimeter = calculate_metrics(10, 5)
print(room_area)       # Output: 50
print(room_perimeter)  # Output: 30





# Using Module Aliasing
import math as mt
print(mt.sqrt(16))  # Output: 4.0
j
# Using Direct Asset Extraction
from math import sqrt
print(sqrt(25))     # Output: 5.0