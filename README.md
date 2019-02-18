This project is very much in development. Pull requests are welcomed.  


# WorkdayWSClient

WorkdayWSClient is a library that aims to simplify the use of Workday Web Services. Objects should be intuitive to use, 
fetch and update data in Workday. The complexities in handling webservices should be removed from the developer. 

While the project is written in Groovy any Java (JDK) language can load the class files (Jar) and use the objects. 

![Reduced Code]()

## Examples
Finding an Academic Appointee by WID

```$xslt
String wid = "0111d20206d2015222b0a3d7f08a15df"
AcadmeicAppointee person = AcademicAppointee.findById(wid)
println person

```

Finding an Academic Appointee by Custom Reference ID

```$xslt
AcadmeicAppointee person = AcademicAppointee.findById("ID123456", "Custom_Ref_ID")
println person

```

Get all workers

```$xslt
List<Worker> people = Worker.findAll()

```

Update Legal Name

```$xslt
AcadmeicAppointee person = AcademicAppointee.findById(wid)
person.legalName.firstName = "New First Name"
person.legalName.middleName = "New Middle Name"
person.legalName.lastName = "New Last Name"
person.save()
```

Update workEmail

```$xslt
Worker person = Worker.findById(wid)
person.workEmail.address = "a" + person.workEmail.address
person.save()
```

# Sample Config
```$xslt
username = "wd_username"
password = 'your_password'
tenant = "your_tenant"
version = "30.2"
host = "your-impl-services1.workday.com"

HRPerson.default.id.type = "WID"

wdUser = "${username}@${tenant}"

```

# Helpful tips for debugging
Run with to see soap messages
```$xslt
-Djavax.net.debug=all

```