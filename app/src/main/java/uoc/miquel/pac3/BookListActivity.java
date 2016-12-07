package uoc.miquel.pac3;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import uoc.miquel.pac3.model.BookContent;
import uoc.miquel.pac3.model.CommonConstants;

/**
 * An activity representing a list of Books. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BookDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BookListActivity extends AppCompatActivity {

    private final static String TAG = "BookListActivity";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private SimpleItemRecyclerViewAdapter adapter;
    private DatabaseReference myRef;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_book_list);

        /**
         * Firebase. Gets the reference of the database and authenticates.
         */
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("books");

        mAuth.signInWithEmailAndPassword("miqasals@gmail.com", "123456")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            downloadBooks();
                        } else {
                            Toast.makeText(BookListActivity.this, "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


        // Floating Action Button.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Swipe Container
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshBookList();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // RecyclerView. Get the reference of the view objects and inflates the list
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.book_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        setupRecyclerView(recyclerView);

        if (findViewById(R.id.book_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }////////////// onCreate


    /**
     * Called when the activity is inititated with the SINGLE_TOP flag set. The intents lauched with this
     * flag only can be processed in this method (not getIntent())
     * @param intent Intent received.
     */
    @Override
    protected void onNewIntent(Intent intent) {

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (intent != null && intent.getAction() != null  && intent.hasExtra(CommonConstants.POSITION_KEY)) {

            // Action DETAIL intent
            if (intent.getAction().equalsIgnoreCase(CommonConstants.ACTION_DETAIL)) {
                int receivedPosition = intent.getIntExtra(CommonConstants.POSITION_KEY, -1);
                if (receivedPosition > -1 && receivedPosition < BookContent.getBooks().size()) {
                    viewBook(receivedPosition);
                    nm.cancel(CommonConstants.NOTIFICATION_ID);
                }

            // Action ERASE intent
            } else if (intent.getAction().equalsIgnoreCase(CommonConstants.ACTION_ERASE)) {
                int receivedPosition = intent.getIntExtra(CommonConstants.POSITION_KEY, -1);
                if (receivedPosition > -1 && receivedPosition < BookContent.getBooks().size()) {
                    removeBook(receivedPosition);
                    nm.cancel(CommonConstants.NOTIFICATION_ID);
                }
            }
            // If the action is MAIN only will load the list.
        }
        

    }

    /**
     * Download the books from Firebase. Called once the application authenticates
     * successfully in remote database.
     */
    private void downloadBooks() {
        swipeContainer.setRefreshing(true);

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange");
                if (dataSnapshot != null) {
                    getBooksFromDataSnapshot(dataSnapshot);
                }
                myRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                getBooksFromDB();
                myRef.removeEventListener(this);
            }
        });
    }


    private void refreshBookList() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange");
                if (dataSnapshot != null) {
                    getBooksFromDataSnapshot(dataSnapshot);
                }
                myRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
                getBooksFromDB();
                myRef.removeEventListener(this);
            }
        });
    }


    /**
     * Get the books from the local database and refresh the list view.
     */
    private void getBooksFromDB() {
        List<BookContent.BookItem> values = BookContent.getBooks();
        adapter.setItems(values);
        swipeContainer.setRefreshing(false);
    }

    /**
     * Parse the list of books received from Firebase, save them in the local database and
     * refresh the list view. If the received data are not correct or unreadable the function
     * get the books from local database and refresh the list.
     *
     * @param dataSnapshot Obtained from Firebase in onDataChange() function.
     */
    private void getBooksFromDataSnapshot(DataSnapshot dataSnapshot) {
        // This method is called once with the initial value and again
        // whenever data at this location is updated.
        GenericTypeIndicator<ArrayList<BookContent.BookItem>> genericTypeIndicator =
                new GenericTypeIndicator<ArrayList<BookContent.BookItem>>() {};
        ArrayList<BookContent.BookItem> values = dataSnapshot.getValue(genericTypeIndicator);
        if (values != null) {

            // Save data in database
            for (BookContent.BookItem bookItem : values) {
                if (!BookContent.exists(bookItem)) {
                    bookItem.save();
                }
            }


            /*
            BookContent.BookItem.deleteAll(BookContent.BookItem.class);
            for (BookContent.BookItem book : values) {
                book.save();
            }
            */

            adapter.setItems(BookContent.getBooks());
            swipeContainer.setRefreshing(false);
        } else {
            getBooksFromDB();
        }
    }

    /**
     * Configure the recycler view adapter and call the inflater method setAdapter().
     * @param recyclerView View object reference
     */
    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new SimpleItemRecyclerViewAdapter(new ArrayList<BookContent.BookItem>());
        recyclerView.setAdapter(adapter);
    }


    /**
     * Start the detail activity or replace the detail fragment depending if the display shows the detail fragment
     * or not. Called when the user tap to the DETAIL button of the notification received or when the user tap
     * over an element from the list.
     * @param position Position in the list of the element to modify.
     */
    public void viewBook(int position) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putInt(BookDetailFragment.ARG_ITEM_ID, position);
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.book_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra(BookDetailFragment.ARG_ITEM_ID, position);
            startActivity(intent);
        }

    }

    /**
     * Erase the referenced book from the local database. Called when the user tap to the DETAIL button of the
     * notification received.
     * @param position Position in the list of the element to modify.
     */
    public void removeBook (int position) {

        BookContent.BookItem book = BookContent.getBooks().get(position);
        book.delete();
        Toast.makeText(this,book.getTitle() + " ELIMINADO", Toast.LENGTH_SHORT).show();
        adapter.setItems(BookContent.getBooks());

        /*
        ArrayList<BookContent.BookItem> books = (ArrayList<BookContent.BookItem>) BookContent.getBooks();
        BookContent.BookItem.deleteAll(BookContent.BookItem.class);
        books.remove(position);
        for (BookContent.BookItem book : books) {
            book.save();
        }
        adapter.setItems(books);
        */
    }








    //////////////////////// RECYCLER ADAPTER ////////////////////////
    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private List<BookContent.BookItem> mValues;
        private final static int EVEN = 0;
        private final static int ODD = 1;

        public SimpleItemRecyclerViewAdapter(List<BookContent.BookItem> items) {
            mValues = items;
        }

        public void setItems(List<BookContent.BookItem> items) {
            mValues = items;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            int type;
            if (position % 2 == 0) {
                type = EVEN;
            } else {
                type = ODD;
            }
            return type;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            if (viewType == EVEN) {
                 view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.book_list_content, parent, false);
            } else if (viewType == ODD) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.book_list_content_odd, parent, false);
            }
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mItem = mValues.get(position);
            holder.mTitleView.setText(mValues.get(position).title);
            holder.mAuthorView.setText(mValues.get(position).author);

            holder.mView.setTag(position);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentPos = (int) v.getTag();
                    /*
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putInt(BookDetailFragment.ARG_ITEM_ID, currentPos);
                        BookDetailFragment fragment = new BookDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.book_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, BookDetailActivity.class);
                        intent.putExtra(BookDetailFragment.ARG_ITEM_ID, currentPos);
                        context.startActivity(intent);
                    }
                    */
                    viewBook(currentPos);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mTitleView;
            public final TextView mAuthorView;
            public BookContent.BookItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitleView = (TextView) view.findViewById(R.id.title);
                mAuthorView = (TextView) view.findViewById(R.id.author);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTitleView.getText() + "'";
            }
        }
    } ///////////// End RECYCLER ADAPTER



}
