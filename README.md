swagger-jaxb
============

JAXB XJC Plugin for automatically adding annotations from Swagger to generated classes from an XSD

Tests run in separate project, see here for the code https://github.com/redlab/swagger-jaxb-tck

What does it do?
================
When you are using XSD to generate your DTO classes you may want to annotate them to make it as detailed as possible for the clients whose are going to use your endpoints.  
The XSD to Java classes generation can be extended with this tool to make your API objects as declarative as they can be.  
The generated classes and methods will be annotated with the proper Swagger annotation, classes with ```@ApiModel``` and methods with ```@ApiModelProperty```.


Example
=======
Example XSD object:
```
 <xsd:complexType name="LoginRequestType">
        <xsd:annotation>
            <xsd:documentation xml:lang="en">Login object containing the e-mail and password</xsd:documentation>
        </xsd:annotation>
        <xsd:complexContent>
                <xsd:sequence>
                    <xsd:element name="email" type="xsd:string">
                        <xsd:annotation>
                            <xsd:documentation xml:lang="en">E-mail of the user</xsd:documentation>
                        </xsd:annotation>
                    </xsd:element>
                    <xsd:element name="password" type="xsd:string">
                        <xsd:annotation>
                            <xsd:documentation xml:lang="en">Password of the user with SHA512 encoding</xsd:documentation>
                        </xsd:annotation>
                    </xsd:element>
                </xsd:sequence>
        </xsd:complexContent>
    </xsd:complexType>
```

Classes
=======
After the class generation the class will be annotated with the ```@ApiModel``` annotation and if the XSD object contains the ```<xsd:documentation>``` tag, the ```description``` attribute of the annotation will be populated.
```
@ApiModel(value = "LoginRequestType", description = "Login object containing the e-mail and password")
public class LoginRequestType {
...
}
```

Fields and methods
==================
```
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(value = "LoginRequestType", description = "Login object containing the e-mail and password")
public class LoginRequestType {

    private final static long serialVersionUID = 0L;
    @XmlElement(required = true)
    protected String email;
    @XmlElement(required = true)
    protected String password;

    @ApiModelProperty(value = "E-mail of the user", required = true, position = 0)
    public String getEmail() {
        return email;
    }

    @ApiModelProperty(value = "Password of the user with SHA512 encoding", required = true, position = 1)
    public String getPassword() {
        return password;
    }
        
    ...
}
```

Because the ```XmlAccessorType``` is FIELD then the getter methods will be annotated with the ```@ApiModelProperty``` annotations, but if the ```XmlAccessType``` is something different, for example PROPERTY or PUBLIC_MEMBER then the fields will be annotated.

ApiModelProperty attributes and their relation to the XSD objects and attributes
================================================================================
In the XSD schemas we are able to define restrictions for simple types and these restrictions should be detailed to the clients. With this plugin these restrictions could be shown to the world by populating the proper attributes of the ```@ApiModelProperty``` annotation.

ApiModelProperty.required
=========================

A field could be marked required which is represented in the  ```io.swagger.annotations.ApiModelProperty.required``` field, if you are willing to make a field required or optional you have to change the value of the ```minOccurs``` tag like in the example:

**Rules:**

| required| minOccurs|
|---------|----------|
| 0       | false    |
|   1     | true     |

```
    <xsd:complexType name="UserType">
        <xsd:annotation>
            <xsd:documentation xml:lang="en">Class represents a user</xsd:documentation>
        </xsd:annotation>
        <xsd:sequence>
            <xsd:element name="firstName" type="xsd:string">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en">First name of the user</xsd:documentation>
                </xsd:annotation>
            </xsd:element>
            <xsd:element name="lastName" type="xsd:string" minOccurs="0">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en">Last name of the user</xsd:documentation>
                </xsd:annotation>
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>
```
Generated class:
```
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserType", propOrder = {
    "firstName",
    "lastName"
})
@ApiModel(value = "UserType", description = "Class represents a user")
public class UserType {

    @XmlElement(required = true)
    protected String firstName;
    protected String lastName;

    @ApiModelProperty(value = "First name of the user", required = true, allowableValues = "range(-infinity, infinity)", position = 0)
    public String getFirstName() {
        return firstName;
    }

    @ApiModelProperty(value = "Last name of the user", allowableValues = "range(-infinity, infinity)", position = 1)
    public String getLastName() {
        return lastName;
    }
    
     ...
    //setters and other javadocs are omitted
    
}
```


ApiModelProperty.allowableValues
================================

The values of the ```<xsd:restriction>``` tags will be populated to the ```io.swagger.annotations.ApiModelProperty.allowableValues``` field.

**Length based restrictions rules:**

| values        | minLength | maxLength | length   |
|---------------|-----------|-----------|----------|
| not-present   | -infinity | infinity  | infinity |
|   present     | value     | value     | value    |

| range     | minLength      | maxLength      | length               |
|-----------|----------------|----------------|----------------------|
| minLength | N/A            | range[min,max] | N/A                  |
| maxLength | range[min,max] | N/A            | N/A                  |
| length    | N/A            | N/A            | range[length,length] |


Length based restrictions example:
```
 <xsd:simpleType name="String100-255LengthType">
        <xsd:restriction base="xsd:string">
            <xsd:minLength value="100"/>
            <xsd:maxLength value="255"/>
        </xsd:restriction>
    </xsd:simpleType>

    <xsd:simpleType name="String255LengthType">
        <xsd:restriction base="xsd:string">
            <xsd:maxLength value="255"/>
        </xsd:restriction>
    </xsd:simpleType>

    <xsd:simpleType name="UUIDType">
        <xsd:restriction base="xsd:string">
            <xsd:length value="36"/>
        </xsd:restriction>
    </xsd:simpleType>
    
    <xsd:complexType name="OrderType">
        <xsd:annotation>
            <xsd:documentation xml:lang="en">Class represents an order</xsd:documentation>
        </xsd:annotation>
        <xsd:sequence>
            <xsd:element name="uuid" type="UUIDType">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en">Order UUID</xsd:documentation>
                </xsd:annotation>
            </xsd:element>
            <xsd:element name="address" type="String255LengthType">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en">Address to send the order</xsd:documentation>
                </xsd:annotation>
            </xsd:element>
            <xsd:element name="cargoIdentifier" type="String100-255LengthType">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en">Identifier of the order cargo</xsd:documentation>
                </xsd:annotation>
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>
```
Generated class:

```
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderType", propOrder = {
    "uuid",
    "address",
    "cargoIdentifier"
})
@ApiModel(value = "OrderType", description = "Class represents an order")
public class OrderType {

    @XmlElement(required = true)
    protected String uuid;
    @XmlElement(required = true)
    protected String address;
    @XmlElement(required = true)
    protected String cargoIdentifier;

    @ApiModelProperty(value = "Order UUID", required = true, allowableValues = "range[36, 36]", position = 0)
    public String getUuid() {
        return uuid;
    }

    @ApiModelProperty(value = "Address to send the order", required = true, allowableValues = "range[-infinity, 255]", position = 1)
    public String getAddress() {
        return address;
    }

    @ApiModelProperty(value = "Identifier of the order cargo", required = true, allowableValues = "range[100, 255]", position = 2)
    public String getCargoIdentifier() {
        return cargoIdentifier;
    }

    ...
    //setters and other javadocs are omitted
}
```

Number related restrictions example:
```
 <xsd:simpleType name="FeedbackStarType">
        <xsd:restriction base="xsd:decimal">
            <xsd:minInclusive value="0"/>
            <xsd:maxInclusive value="10"/>
        </xsd:restriction>
    </xsd:simpleType>

    <xsd:simpleType name="FeedbackExclusiveStarType">
        <xsd:restriction base="xsd:decimal">
            <xsd:minExclusive value="-1"/>
            <xsd:maxExclusive value="11"/>
        </xsd:restriction>
    </xsd:simpleType>

    <xsd:simpleType name="FeedbackMixedStarType">
        <xsd:restriction base="xsd:decimal">
            <xsd:minExclusive value="-1"/>
            <xsd:maxInclusive value="10"/>
        </xsd:restriction>
    </xsd:simpleType>
    
     <xsd:complexType name="FeedbackType">
        <xsd:annotation>
            <xsd:documentation xml:lang="en">Class represents a feedback</xsd:documentation>
        </xsd:annotation>
        <xsd:sequence>
            <xsd:element name="simpleFeedback" type="FeedbackStarType">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en">Simple feedback</xsd:documentation>
                </xsd:annotation>
            </xsd:element>
            <xsd:element name="exclusiveFeedback" type="FeedbackExclusiveStarType">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en">Feedback with exclusive ranges</xsd:documentation>
                </xsd:annotation>
            </xsd:element>
            <xsd:element name="mixedFeedback" type="FeedbackMixedStarType">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en">Mixed feedback with inclusive and exclusive values</xsd:documentation>
                </xsd:annotation>
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>
```
Generated class:
```
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeedbackType", propOrder = {
    "simpleFeedback",
    "exclusiveFeedback",
    "mixedFeedback"
})
@ApiModel(value = "FeedbackType", description = "Class represents a feedback")
public class FeedbackType {

    @XmlElement(required = true)
    protected BigDecimal simpleFeedback;
    @XmlElement(required = true)
    protected BigDecimal exclusiveFeedback;
    @XmlElement(required = true)
    protected BigDecimal mixedFeedback;
    
    @ApiModelProperty(value = "Simple feedback", required = true, allowableValues = "range[0, 10]", position = 0)
    public BigDecimal getSimpleFeedback() {
        return simpleFeedback;
    }

    @ApiModelProperty(value = "Feedback with exclusive ranges", required = true, allowableValues = "range(-1, 11)", position = 1)
    public BigDecimal getExclusiveFeedback() {
        return exclusiveFeedback;
    }

    @ApiModelProperty(value = "Mixed feedback with inclusive and exclusive values", required = true, allowableValues = "range(-1, 10]", position = 2)
    public BigDecimal getMixedFeedback() {
        return mixedFeedback;
    }
    
    ...
    //setters and other javadocs are omitted
}
```
As you can see the ```allowableValues``` attribue is populated everywhere with different values and brackets:  
```(``` - minExclusive  
```)``` - maxExclusive  
```[``` - minInclusive  
```]``` - maxInclusive

They could come handy in sometimes.

Enum related generations
```
 <xsd:simpleType name="UserStatusType">
        <xsd:annotation>
            <xsd:documentation xml:lang="en">Status of the user</xsd:documentation>
        </xsd:annotation>
        <xsd:restriction base="xsd:string">
            <xsd:enumeration value="ACTIVE"/>
            <xsd:enumeration value="INACTIVE"/>
        </xsd:restriction>
    </xsd:simpleType>
    
     <xsd:complexType name="NewUserType">
        <xsd:annotation>
            <xsd:documentation xml:lang="en">Class represents a new user</xsd:documentation>
        </xsd:annotation>
        <xsd:sequence>
            <xsd:element name="status" type="UserStatusType">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en">Status of the user</xsd:documentation>
                </xsd:annotation>
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>
```

Generated class:
```
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NewUserType", propOrder = {
    "status"
})
@ApiModel(value = "NewUserType", description = "Class represents a new user")
public class NewUserType {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected UserStatusType status;
    
    @ApiModelProperty(value = "Status of the user", required = true, allowableValues = "ACTIVE, INACTIVE", position = 0)
    public UserStatusType getStatus() {
        return status;
    }
    
    ...
    //setters and other javadocs are omitted
}

```
The values of an enum will be populated to the ```allowableValues``` tag with a ```,``` between them.

ApiModelProperty.example
========================
Sometimes you would like to create an example value for a field, maybe for quick testing purposes or for some clarification, for example you put a JWT token into the example and if somebody parses it, they will be able to see what does a JWT token contains.  
You can specify an example with the following:

```
    <xsd:complexType name="LoginResponse">
        <xsd:annotation>
            <xsd:documentation xml:lang="en">Class represents a login response</xsd:documentation>
        </xsd:annotation>
        <xsd:sequence>
            <xsd:element name="token" type="xsd:string" default="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en">Generated JWT token</xsd:documentation>
                </xsd:annotation>
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>
```
Generated class:
```
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoginResponse", propOrder = {
    "token"
})
@ApiModel(value = "LoginResponse", description = "Class represents a login response")
public class LoginResponse {

    @XmlElement(required = true, defaultValue = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    protected String token;

    @ApiModelProperty(value = "Generated JWT token", required = true, example = "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"", position = 0)
    public String getToken() {
        return token;
    }
    
    ...
    //setters and other javadocs are omitted

}
```
Examples could come handy in some extreme cases like with weird identifiers or with JWT tokens.

You maybe wondering why the ```position``` attribute is filled, because it can help you to organize an example request on the Swagger UI, every field you present in the XSD object, they will be ordered and in the annotation the ```position``` attribute will be populated with the proper position value. It respects the inheritance as well, look at the example:
```
<xsd:complexType name="SuperDuperClass">
        <xsd:annotation>
            <xsd:documentation xml:lang="en">SuperDuperClass</xsd:documentation>
        </xsd:annotation>
        <xsd:sequence>
            <xsd:element name="superDuperClassField" type="xsd:string">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en">Super duper class field</xsd:documentation>
                </xsd:annotation>
            </xsd:element>
        </xsd:sequence>
    </xsd:complexType>

    <xsd:complexType name="SuperClass">
        <xsd:annotation>
            <xsd:documentation xml:lang="en">SuperClass</xsd:documentation>
        </xsd:annotation>
        <xsd:complexContent>
            <xsd:extension base="SuperDuperClass">
                <xsd:sequence>
                    <xsd:element name="superClassField" type="xsd:string">
                        <xsd:annotation>
                            <xsd:documentation xml:lang="en">Super class field</xsd:documentation>
                        </xsd:annotation>
                    </xsd:element>
                </xsd:sequence>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>

    <xsd:complexType name="DerivedClass">
        <xsd:annotation>
            <xsd:documentation xml:lang="en">DerivedClass</xsd:documentation>
        </xsd:annotation>
        <xsd:complexContent>
            <xsd:extension base="SuperClass">
                <xsd:sequence>
                    <xsd:element name="derivedClassField" type="xsd:string">
                        <xsd:annotation>
                            <xsd:documentation xml:lang="en">Derived class field</xsd:documentation>
                        </xsd:annotation>
                    </xsd:element>
                </xsd:sequence>
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>
```
Generated classes:
```
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SuperDuperClass", propOrder = {
    "superDuperClassField"
})
@XmlSeeAlso({
    SuperClass.class
})
@ApiModel(value = "SuperDuperClass", description = "SuperDuperClass")
public class SuperDuperClass {

    @XmlElement(required = true)
    protected String superDuperClassField;

    @ApiModelProperty(value = "Super duper class field", required = true, position = 0)
    public String getSuperDuperClassField() {
        return superDuperClassField;
    }
}


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SuperClass", propOrder = {
    "superClassField"
})
@XmlSeeAlso({
    DerivedClass.class
})
@ApiModel(value = "SuperClass", description = "SuperClass")
public class SuperClass
    extends SuperDuperClass
{

    @XmlElement(required = true)
    protected String superClassField;

    @ApiModelProperty(value = "Super class field", required = true, position = 1)
    public String getSuperClassField() {
        return superClassField;
    }
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DerivedClass", propOrder = {
    "derivedClassField"
})
@ApiModel(value = "DerivedClass", description = "DerivedClass")
public class DerivedClass
    extends SuperClass
{

    @XmlElement(required = true)
    protected String derivedClassField;

    @ApiModelProperty(value = "Derived class field", required = true, position = 2)
    public String getDerivedClassField() {
        return derivedClassField;
    }
}
```

```position``` attribute are filled continuously.

These examples could be found in the module named: ```swagger-jaxb-examples```

Missing stuff
=============
Unfortunately the used Swagger annotations dependency which is based on the Swagger 2.0 specification does not support the ```pattern``` attribute to populate, but it could have been useful for some developers. Hope that the upcoming specs will support that :)

Usage
============
* REQUIRE Java 8 or higher! 
* build the plugin with maven
* install it in your local repo
* add the plugin to your classpath and use -swaggify on your jaxb command line or configure it i your pom
or
* add sonatype snapshot repository to your repo manager. ( post an issue if you really want dev version in Maven Central )
 
use with jaxb2-maven-plugin 

```
    <build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>jaxb2-maven-plugin</artifactId>
                <version>2.3</version>
                <dependencies>
                    <dependency>
                        <groupId>co.rodnan</groupId>
                        <artifactId>swagger-jaxb</artifactId>
                        <version>1.0</version>
                    </dependency>
                    <dependency>
                        <groupId>javax.xml.parsers</groupId>
                        <artifactId>jaxp-api</artifactId>
                        <version>1.4.5</version>
                    </dependency>
                    <dependency>
                        <groupId>com.sun.xml.parsers</groupId>
                        <artifactId>jaxp-ri</artifactId>
                        <version>1.4.5</version>
                    </dependency>
                    <dependency>
                        <groupId>com.sun.xml.bind</groupId>
                        <artifactId>jaxb-xjc</artifactId>
                        <version>2.2.11</version>
                    </dependency>
                    <dependency>
                        <groupId>com.sun.xml.bind</groupId>
                        <artifactId>jaxb-core</artifactId>
                        <version>2.2.11</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </pluginManagement>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>jaxb2-maven-plugin</artifactId>
            <version>2.3</version>
            <executions>
                    <execution>
                        <id>internal.generate</id>
                        <goals>
                            <goal>xjc</goal>
                        </goals>
                        <configuration>
                            <arguments>-swaggerify</arguments>
                            <clearOutputDir>true</clearOutputDir>
                            <packageName>be.redlab.jaxb.swagger.generated.model</packageName>
                            <sources>
                                <source>${project.basedir}/src/main/xsd/schema</source>
                            </sources>
                        </configuration>
                    </execution>
            </executions>
        </plugin>
	</plugins>

    <dependencies>
        <dependency>
            <groupId>io.swagger</groupId>
            <artifactId>swagger-annotations</artifactId>
            <version>1.5.12</version>
        </dependency>
    </dependencies>
```

