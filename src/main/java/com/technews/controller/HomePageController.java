package com.technews.controller;

import com.technews.model.Comment;
import com.technews.model.Post;
import com.technews.model.User;
import com.technews.repository.CommentRepository;
import com.technews.repository.PostRepository;
import com.technews.repository.UserRepository;
import com.technews.repository.VoteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

//use controller to establish entry and exit points
@Controller
public class HomePageController {
//    set up relationships for repositories
    @Autowired
UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    CommentRepository commentRepository;

//    login template is rendered when /login route is hit
    @GetMapping("/login")
//    remap Model to model variable
    public String login(Model model, HttpServletRequest request) {

        if (request.getSession(false) != null) {
            return "redirect:/";
        }
// addAttribute method which is built into the model object, sends the info to the Thymeleaf templates
        model.addAttribute("user", new User());
//        newly created user is sent to the template as a string to be displayed within the template
        return "login";
    }

    @GetMapping("/users/logout")
//    when logout route is hit, session is invalidated - user is redirected
    public String logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
        return "redirect:/login";
    }

    @GetMapping("/")
    public String homepageSetup(Model model, HttpServletRequest request) {
        User sessionUser = new User();

        if (request.getSession(false) != null) {
            sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        } else {
            model.addAttribute("loggedIn", false);
        }

//retrieve post data
        List<Post> postList = postRepository.findAll();
        for (Post p : postList) {
            p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
            User user = userRepository.getById(p.getUserId());
            p.setUserName(user.getUsername());
        }
// add details to the user model and populate on homepage
        model.addAttribute("postList", postList);
        model.addAttribute("loggedIn", sessionUser.isLoggedIn());

        // "point" and "points" attributes refer to upvotes.
        model.addAttribute("point", "point");
        model.addAttribute("points", "points");

        return "homepage";
    }

    @GetMapping("/dashboard")
    public String dashboardPageSetup(Model model, HttpServletRequest request) throws Exception {
        if (request.getSession(false) != null) {
            setupDashboardPage(model, request);
            return "dashboard";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }
    @GetMapping("/dashboardEmptyTitleAndLink")
    public String dashboardEmptyTitleAndLinkHandler(Model model, HttpServletRequest request) throws Exception {
        setupDashboardPage(model, request);
        model.addAttribute("notice", "To create a post the Title and Link must be populated!");
        return "dashboard";
    }
    @GetMapping("/singlePostEmptyComment/{id}")
    public String singlePostEmptyCommentHandler(@PathVariable int id, Model model, HttpServletRequest request) {
        setupSinglePostPage(id, model, request);
        model.addAttribute("notice", "To add a comment you must enter the comment in the comment text area!");
        return "single-post";
    }
    @GetMapping("/post/{id}")
    public String singlePostPageSetup(@PathVariable int id, Model model, HttpServletRequest request) {
        setupSinglePostPage(id, model, request);
        return "single-post";
    }
    @GetMapping("/editPostEmptyComment/{id}")
    public String editPostEmptyCommentHandler(@PathVariable int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            setupEditPostPage(id, model, request);
            model.addAttribute("notice", "To add a comment you must enter the comment in the comment text area!");
            return "edit-post";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }
    @GetMapping("/dashboard/edit/{id}")
    public String editPostPageSetup(@PathVariable int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            setupEditPostPage(id, model, request);
            return "edit-post";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }
    public Model setupDashboardPage(Model model, HttpServletRequest request) throws Exception {
//        assign current user session as sessionUser
        User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
        Integer userId = sessionUser.getId();
//        find all posts by users id - for loop gathers all posts
        List<Post> postList = postRepository.findAllPostsByUserId(userId);
        for (Post p : postList) {
            p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
            User user = userRepository.getById(p.getUserId());
            p.setUserName(user.getUsername());
        }
//        sending current user session to the Thymeleaf dashboard as "user"
        model.addAttribute("user", sessionUser);
        model.addAttribute("postList", postList);
        model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        model.addAttribute("post", new Post());
        return model;
    }
    public Model setupSinglePostPage(int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            model.addAttribute("sessionUser", sessionUser);
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        }
        Post post = postRepository.getById(id);
        post.setVoteCount(voteRepository.countVotesByPostId(post.getId()));
        User postUser = userRepository.getById(post.getUserId());
        post.setUserName(postUser.getUsername());
        List<Comment> commentList = commentRepository.findAllCommentsByPostId(post.getId());
        model.addAttribute("post", post);
        model.addAttribute("commentList", commentList);
        model.addAttribute("comment", new Comment());
        return model;
    }
    public Model setupEditPostPage(int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            Post returnPost = postRepository.getById(id);
            User tempUser = userRepository.getById(returnPost.getUserId());
            returnPost.setUserName(tempUser.getUsername());
            returnPost.setVoteCount(voteRepository.countVotesByPostId(returnPost.getId()));
            List<Comment> commentList = commentRepository.findAllCommentsByPostId(returnPost.getId());
            model.addAttribute("post", returnPost);
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
            model.addAttribute("commentList", commentList);
            model.addAttribute("comment", new Comment());
        }
        return model;
    }
    }



