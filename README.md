# Payment Tracker
This project contains an implementation of a "Payment Tracker" according to the 
"HOMEWORK FOR BACKEND DEVELOPER POSITION IN BSC" specification.

## Assumptions
* The amount in an input line can be an integer or a decimal number with up to two decimal places 
* Leading and trailing whitespaces in an input line are ignored 
  (e.g. `  USD -55.5  ` is considered as valid input)
* Malformed input line, either in the input file or typed in console, is reported as an error 
  message to standard the error output and ignored (e.g. `USD 55.125` is considered as invalid input)
* All but first argument passed to the application during the start are silently ignored
* When printing the net amounts, the currencies are listed in the alphabetical order  

## Missing features
* The "Optional bonus" feature allowing each currency to have the exchange rate compared to USD 
  isn't yet implemented.

## Note
File and/or folder paths in this README are using unix notation (i.e. `/` for folder separation). 
When using other OS (typically Windows), please adjust the paths accordingly.

Last but not least, unless stated otherwise, when referring to a command line it's 
assumed that the current working folder is the root of the payment tracker project 
(i.e. the same folder where this README is located)

## Building the "Payment Tracker"

### Prerequisites
 - JDK 11.x (or higher)
 - Maven 3.x
 
### Instructions
The stand-alone application can be built from the command line by executing
 
`mvn clean package` 
 
Once the aforementioned command completes, the stand-alone binary (a.k.a. "FatJar") will appear in the
`./target` folder.
 
## Running the "Payment Tracker"
 
### Prerequisites
  - JRE 11.x (or higher)
  - Payment Tracker application "FatJar" (payment-tracker-1.0.jar)

### Instructions
The application should be started from the command line by executing

`java -jar target/payment-tracker-1.0.jar`

It's possible to specify an optional "payment entry" file by passing its path as an argument 
to the application. 

`java -jar target/payment-tracker-1.0.jar sample-input.txt`

### Disable "verbose" mode
By default, the application starts in a "verbose" mode printing additional information (e.g. file
loading stats, input prompt message, header for net currency amount summaries). This behaviour
can be suppressed by overriding the value of the system property called `payment.tracker.verbose`. 
This could be done via command line by executing

`java -Dpayment.tracker.verbose=false -jar target/payment-tracker-1.0.jar`

This option will also work in combination with the "payment entry" file argument, just make sure
the system property override is passed as a first argument to the `java` executable. A command line
can look like this

`java -Dpayment.tracker.verbose=false -jar target/payment-tracker-1.0.jar sample-input.txt`

