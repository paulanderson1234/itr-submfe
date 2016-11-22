# Investment Tax Relief Submission Frontend

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)[
![Build Status](https://api.travis-ci.org/hmrc/investment-tax-relief-submission-frontend.svg?branch=master)](https://travis-ci.org/hmrc/investment-tax-relief-submission-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/investment-tax-relief-submission-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/investment-tax-relief-submission-frontend/_latestVersion)


Overview - Investment Tax Relief 
--------------------------------

Tax Advantaged Venture Capital Schemes (TAVC) include The Enterprise Investment Scheme (EIS), Seed Enterprise Investment Scheme (SEIS), Social Investment Tax Relief (SITR) and Venture Capital Trusts (VCT). 

The EIS and SEIS schemes encourage equity investment in small and medium size companies that have traditionally struggled to obtain financing whilst the newer SITR scheme aims to encourage equity and debt investment in socially beneficial projects. EIS, SEIS and SITR schemes incentivise investment by giving tax reliefs (both Income Tax and Capital Gains Tax) to qualifying individual investors. These schemes attract a high level of political interest with their use actively encouraged by a number of Government Ministers and politicians and are a part of the Government's growth and 'Big Society' agendas. VCT operates a similar process though the customer base is Investment trust driven.

The user population is made up of mainly UK based, but some non-UK based limited companies and charities and agents acting on their behalf.

The investment tax relief services are the represent the realisation of a digital service the above.
Features will be added and enhanced over time.    
  

Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.


## Run the application


To update from Nexus and start all services from the RELEASE version instead of snapshot

```
sm --start TAVC_ALL -f
```

It is also possible to start just our microservice without any of microservices themselves by issuing the below instead:
 
```
sm --start TAVC_DEP -f
```

##To run the application locally execute the following:

Kill the service  ```sm --stop ITR_SUBM_FE``` then run:
```
sbt 'run 9635' 
```


You can _*optionally*_ also run any of our other dependent microservices locally by killing the services specified and running the commands as shown below:


[Submission Microservice](https://github.com/hmrc/investment-tax-relief-submission)

Kill the service  ```sm --stop ITR_SUBM``` in service Manager and run:
```
sbt 'run 9636'
```

[Submission Stub](https://github.com/hmrc/investment-tax-relief-submission-dynamic-stub/)


Kill the service ```sm --stop ITR_SUBM_DYNAMIC_STUB``` in service Manager and run:
```
sbt 'run 9639' 
```


[Subscription Frontend](https://github.com/hmrc/investment-tax-relief-subscription-frontend)

Kill the service ```sm --stop  ITR_SUBSC_FE``` and run:
```
sbt 'run 9637' 
```
  

[Subscription Microservice](https://github.com/hmrc/investment-tax-relief-subscription)
  
Kill the service ```sm --stop  ITR_SUBSC``` and run:
```
sbt 'run 9638'
```
  
[Subscription Stub](https://github.com/hmrc/investment-tax-relief-submission-dynamic-stub/)

Kill the service ```sm --stop ITR_SUBSC_DYNAMIC_STUB``` and run:  
```
sbt 'run 9640'
```
  

## Test the application

To test the application execute

```
sbt test
```

License
---

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


[JRE]: http://www.oracle.com/technetwork/java/javase/overview/index.html
[API]: https://en.wikipedia.org/wiki/Application_programming_interface
[URL]: https://en.wikipedia.org/wiki/Uniform_Resource_Locator
