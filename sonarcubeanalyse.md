# Impact Faible
+ Remove the 'ApiResponses' wrapper from this annotation group [java:S1710]
+ Remove this unused import 'org.assertj.core.api.Fail.fail'. [java:S1128]
+ Rename this field "Repository" to match the regular expression '^[a-z][a-zA-Z0-9]*$'. [java:S116]
+ Rename this generic name to match the regular expression '^[A-Z][0-9]?$'. [java:S119]
+ ID is not used in the interface. [java:S2326]
+ Rename this bean to match the regular expression '^[a-z][a-zA-Z0-9]*$'. [java:S6830]
+ Add a private constructor to hide the implicit public one. [java:S1118]
+ Remove this empty statement. [java:S1116]

# Impact Moyen
+ Add at least one assertion to this test case. [java:S2699]
+ Explicitly specify the time zone by passing a ZoneId or a Clock to the .now() method. [java:S8688]

# Impact Important
+ Replace this use of System.out by a logger. [java:S106]
+ Replace this persistent entity with a simple POJO or DTO object. [java:S4684]
