# Fun with Stormpath CustomData
#### Oh, yeah - It's also Spring Boot + Spring Security too!

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

## The Story so Far

A bunch of us at Stormpath were talking about our favorite books and one of us thought it would be cool to both have a list of these books and
order them by popularity. He setup a private [subreddit](https://reddit.com) for us to do just that.

Only, some of us here at Stormpath don't have reddit accounts. Some of us, don't even like reddit (not naming names, here).

So I thought: "I wonder how hard it would be to implement this book voting system in Stormpath?" 

Well, the answer is it took a couple of days to create, coding it outside of my normal working time (What does that even mean in a startup?)

## What is Stormpath?

Stormpath is a complete identity API. It gives you powerful authentication, authorization, and user management for any application.

Check out the docs for a variety of platforms [here](https://docs.stormpath.com/home/).

[Here](http://docs.stormpath.com/java/spring-boot-web/quickstart.html) is the Java Quickstart. Follow the Quickstart to create a Stormpath account.
That's all you need to get going.

## What the Heck is CustomData?

Stormpath has up to 10MB of text based `CustomData` attached to every first class Object. For example, Every Stormpath Directory, Group and Account can have
`CustomData` attached to it.

The actual data is schema-less JSON. Ordinarily, this would be data would have something to do with your identity management needs. But, there's no rule book.

## Setup your Stormpath Application and Groups

To exercise this app, you'll need to create a Stormpath Application, as well as an Admin Group and a User Group. Make note of the `href` of each of these.
We'll use them later.

Anyone - even someone who is not logged in - can see the list of books ordere by upvote.

Anyone can create an account.

Only members of the User Group you specify can add new books. For the sake of making the app as hands-off as possible, authenticated users can add themselves to the User Group.

Only members of the Admin Group you specify can rebuild the book list. More on that later.

## Data Structure

We're going to use a particular key so as not to interfere with any other `CustomData` you might want to have. Here's a sample of what it looks like:

```
{
  "books": [
    {
      "author": "Dan Suarez",
      "title": "Daemon",
      "url": "http://www.amazon.com/DAEMON-Daniel-Suarez/dp/0451228731/ref=sr_1_1?ie=UTF8&qid=1447719382&sr=8-1&keywords=daemon",
      "votes": 2
    },
    {
      "author": "Dan Suarez",
      "title": "Freedom (TM)",
      "url": "http://www.amazon.com/Freedom-TM-Daniel-Suarez/dp/0451231899/ref=sr_1_1?ie=UTF8&qid=1447795097&sr=8-1&keywords=freedom%28tm%29",
      "votes": 3
    },
    {
      "author": "Neal Stephenson",
      "title": "Snow Crash",
      "url": "http://www.amazon.com/Snow-Crash-Neal-Stephenson/dp/0553380958/ref=sr_1_1?ie=UTF8&qid=1447677686&sr=8-1&keywords=snow+crash",
      "votes": 3
    },
    {
      "author": "Robert Heinlein",
      "title": "Stranger in a Strange Land",
      "url": "http://www.amazon.com/Stranger-Strange-Land-Remembering-Tomorrow/dp/0441790348/ref=sr_1_1?ie=UTF8&qid=1448304063&sr=8-1&keywords=stranger+in+a+strange+land",
      "votes": 3
    },
    {
      "author": "Neal Stephenson",
      "title": "The Diamond Age",
      "url": "http://www.amazon.com/Diamond-Age-Illustrated-Primer-Spectra/dp/0553380966/ref=sr_1_1?ie=UTF8&qid=1447682975&sr=8-1&keywords=the+diamond+age",
      "votes": 3
    },
    {
      "author": "Andy Weir",
      "title": "The Martian",
      "url": "http://www.amazon.com/Martian-Andy-Weir/dp/0553418025/ref=sr_1_1?ie=UTF8&qid=1447677656&sr=8-1&keywords=the+martian",
      "votes": 3
    },
    {
      "author": "L. Frank Baum",
      "title": "The Wonderful Wizard of Oz",
      "url": "http://www.amazon.com/The-Wonderful-Wizard-Oz-Commemorative/dp/0689817517?ref=spkl_1_0_2279092822&qid=1447826537&pf_rd_p=2279092822&pf_rd_m=ATVPDKIKX0DER&pf_rd_t=301&pf_rd_s=desktop-auto-sparkle&pf_rd_r=0M79F14H25J9FA53Y89C&pf_rd_i=science+fiction+best+sellers",
      "votes": 2
    }
  ]
}
```

The top level key is `books`. It's an array of book data including the number of votes a book has.

The books you submit will be stored in the `CustomData` attached to your Stormpath Account.

Now, here's the tricky bit: The `CustomData` attached to the the User Group you set up earlier will store the aggregated book data. Why?
So that the app is responsive in returning the book data to you. Otherwise, it would need to gather up all the books `CustomData` from 
each member of the User Group and build the book list on the fly.

*Note*: This is not a very scalable app. You would not want to store thousands of books in Stormpath `CustomData`. It is functional and
fun to play with.

## Build and Run

If you want to see this in action right away, use the Deploy button below.

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

To build and run locally, do the following:

```
mvn clean package
STORMPATH_API_KEY_FILE=<path to your apiKey.properties file> \
STORMPATH_APPLICATION_HREF=<full href to your Stormpath Application> \
STORMPATH_AUTHORIZED_GROUP_USER_HREF=<full href to your Stormpath User Group> \
STORMPATH_AUTHORIZED_GROUP_ADMIN_HREF=<full href to your Stormpath Admin Group> \
java -jar target/*.jar
```

## What It Is

Once you are a member of the User Group, you can add new books to the list and you can upvote books from others.

That's pretty much it. There's not a ton of bells and whistles.

## What It Is Not

It's not a SPA (Single Page Application). Making an Angular app out of it would be a fun improvement.

It's not meant to have tons of data. It should be very responsive with a couple of hundred books. It may start to
become less responsive if you have thousands of books. Maybe I'll do a little stress testing to see how it performs
with a ridiculous number of books. I'll keep you posted.
