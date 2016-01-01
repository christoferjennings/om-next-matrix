# om.next with a matrix experiment

This is my experiment to get a matrix data to work with om.next.
I'm just learning om.next so don't expect much.

I'm working in emacs with cider. The build uses [boot](https://github.com/boot-clj/boot).

To run it do....

```
boot dev
```
Then open chrome to [http://localhost:3000/](http://localhost:3000/)

If it works, you should see a 2x2 matrix and a button. Clicking the button
randomly changes one of the cells in the matrix. I want om.next to handle the
mutate so only the affected cell gets re-rendered, but so far the best I can
do is have the whole matrix re-render.
