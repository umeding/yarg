># yarg

Yet Another REST Generator (for Java and Markdown)

## Documentation
* todo

## Notes
The Java formatter creates interfaces with the appropriate
annotatations. This is fine with the exception of the `@Path`
annotation on the interface itself. The <a
href="http://download.oracle.com/otn-pub/jcp/jaxrs-2_0-fr-eval-spec/jsr339-jaxrs-2.0-final-spec.pdf"
target="_blank">JAX-RS spec</a>, Section 3.6, p. 19: 
> The precedence over conflicting annotations defined in
> multiple implemented interfaces is implementation specific.
> Note that inheritance of class or interface annotations
> is not supported.

This means that you need to add `@Path("...")` on the __implementing__
resource to make it work.
