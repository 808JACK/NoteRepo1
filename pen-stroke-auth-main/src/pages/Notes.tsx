import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { BookOpen, Plus, Edit, Trash2, Search } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useToast } from "@/hooks/use-toast";
import ProfileDropdown from "@/components/ProfileDropdown";
import { apiService, Note } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";

const Notes = () => {
  const [notes, setNotes] = useState<Note[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [newNote, setNewNote] = useState({ title: "", content: "" });
  const [editingNote, setEditingNote] = useState<Note | null>(null);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();
  const { toast } = useToast();
  const { user } = useAuth();

  if (!user) {
    navigate("/login");
    return null;
  }
  
  useEffect(() => {
    const loadNotes = async () => {
      try {
        setIsLoading(true);
        const userNotes = await apiService.getNotesByUserId(user.userId.toString());
        setNotes(userNotes);
      } catch (error) {
        console.error('Error loading notes:', error);
        toast({
          title: "Error",
          description: "Failed to load notes. Please try again.",
          variant: "destructive",
        });
      } finally {
        setIsLoading(false);
      }
    };

    loadNotes();
  }, [user.userId, toast]);

  const handleCreateNote = async () => {
    if (!newNote.title.trim() || !newNote.content.trim()) {
      toast({
        title: "Error",
        description: "Please fill in both title and content",
        variant: "destructive",
      });
      return;
    }

    try {
      const noteData: Note = {
        title: newNote.title,
        content: newNote.content,
        userId: user.userId.toString(),
      };

      const createdNote = await apiService.createNote(noteData);
      setNotes(prevNotes => [createdNote, ...prevNotes]);
      setNewNote({ title: "", content: "" });
      setIsCreateOpen(false);
      toast({
        title: "Note Created",
        description: "Your note has been successfully created",
      });
    } catch (error) {
      console.error('Error creating note:', error);
      toast({
        title: "Error",
        description: "Failed to create note. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleUpdateNote = async () => {
    if (!editingNote || !editingNote.title.trim() || !editingNote.content.trim()) {
      toast({
        title: "Error",
        description: "Please fill in both title and content",
        variant: "destructive",
      });
      return;
    }

    try {
      const updatedNote = await apiService.updateNote(editingNote.id!, editingNote);
      setNotes(prevNotes => 
        prevNotes.map(note => 
          note.id === editingNote.id ? updatedNote : note
        )
      );
      setEditingNote(null);
      setIsEditOpen(false);
      toast({
        title: "Note Updated",
        description: "Your note has been successfully updated",
      });
    } catch (error) {
      console.error('Error updating note:', error);
      toast({
        title: "Error",
        description: "Failed to update note. Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleDeleteNote = async (id: string) => {
    try {
      await apiService.deleteNote(id);
      setNotes(prevNotes => prevNotes.filter(note => note.id !== id));
      toast({
        title: "Note Deleted",
        description: "Your note has been successfully deleted",
      });
    } catch (error) {
      console.error('Error deleting note:', error);
      toast({
        title: "Error",
        description: "Failed to delete note. Please try again.",
        variant: "destructive",
      });
    }
  };

  // Search functionality with debouncing would be better, but for now simple filter
  const filteredNotes = searchTerm.trim() 
    ? notes.filter(note =>
        note.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        note.content?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    : notes;

  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return 'Unknown';
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch (error) {
      return 'Invalid date';
    }
  };

  return (
    <div className="min-h-screen gradient-secondary">
      {/* Header */}
      <header className="border-b bg-background/50 backdrop-blur-md">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg gradient-primary shadow-glow">
              <BookOpen className="h-5 w-5 text-white" />
            </div>
            <h1 className="text-xl font-bold text-gradient">My Notes</h1>
          </div>
          <ProfileDropdown username={user.username} email={user.email} />
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        {/* Search and Create */}
        <div className="flex flex-col sm:flex-row gap-4 mb-8">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search notes..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10 transition-smooth focus:shadow-primary/20"
            />
          </div>
          <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
            <DialogTrigger asChild>
              <Button variant="gradient" className="shrink-0">
                <Plus className="mr-2 h-4 w-4" />
                New Note
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle className="text-gradient">Create New Note</DialogTitle>
                <DialogDescription>
                  Add a new note to your collection
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div>
                  <Label htmlFor="title">Title</Label>
                  <Input
                    id="title"
                    placeholder="Enter note title"
                    value={newNote.title}
                    onChange={(e) => setNewNote({ ...newNote, title: e.target.value })}
                    className="transition-smooth focus:shadow-primary/20"
                  />
                </div>
                <div>
                  <Label htmlFor="content">Content</Label>
                  <Textarea
                    id="content"
                    placeholder="Write your note content here..."
                    value={newNote.content}
                    onChange={(e) => setNewNote({ ...newNote, content: e.target.value })}
                    rows={6}
                    className="transition-smooth focus:shadow-primary/20"
                  />
                </div>
                <Button onClick={handleCreateNote} variant="gradient" className="w-full">
                  Create Note
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        </div>

        {/* Loading State */}
        {isLoading ? (
          <div className="text-center py-16">
            <div className="mx-auto mb-4 p-4 rounded-full bg-muted/50 w-16 h-16 flex items-center justify-center">
              <BookOpen className="h-8 w-8 text-muted-foreground animate-pulse" />
            </div>
            <h3 className="text-lg font-semibold mb-2">Loading your notes...</h3>
            <p className="text-muted-foreground">Please wait while we fetch your notes</p>
          </div>
        ) : (
          <>
            {/* Notes Grid */}
            {filteredNotes.length === 0 ? (
          <div className="text-center py-16">
            <div className="mx-auto mb-4 p-4 rounded-full bg-muted/50 w-16 h-16 flex items-center justify-center">
              <BookOpen className="h-8 w-8 text-muted-foreground" />
            </div>
            <h3 className="text-lg font-semibold mb-2">No notes found</h3>
            <p className="text-muted-foreground mb-4">
              {searchTerm ? "Try adjusting your search terms" : "Create your first note to get started"}
            </p>
            {!searchTerm && (
              <Button variant="gradient" onClick={() => setIsCreateOpen(true)}>
                <Plus className="mr-2 h-4 w-4" />
                Create Your First Note
              </Button>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredNotes.map((note) => (
              <Card key={note.id} className="gradient-card shadow-card border-0 hover:shadow-primary/20 hover:scale-105 transition-bounce">
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <CardTitle className="text-lg mb-2 line-clamp-1">{note.title}</CardTitle>
                      <CardDescription className="text-xs">
                        Updated {formatDate(note.updatedAt)}
                      </CardDescription>
                    </div>
                    <div className="flex gap-1">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => {
                          setEditingNote(note);
                          setIsEditOpen(true);
                        }}
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDeleteNote(note.id!)}
                      >
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-sm text-muted-foreground line-clamp-4">
                    {note.content}
                  </p>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
        </>
        )}

        {/* Edit Dialog */}
        <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle className="text-gradient">Edit Note</DialogTitle>
              <DialogDescription>
                Make changes to your note
              </DialogDescription>
            </DialogHeader>
            {editingNote && (
              <div className="space-y-4">
                <div>
                  <Label htmlFor="edit-title">Title</Label>
                  <Input
                    id="edit-title"
                    placeholder="Enter note title"
                    value={editingNote.title}
                    onChange={(e) => setEditingNote({ ...editingNote, title: e.target.value })}
                    className="transition-smooth focus:shadow-primary/20"
                  />
                </div>
                <div>
                  <Label htmlFor="edit-content">Content</Label>
                  <Textarea
                    id="edit-content"
                    placeholder="Write your note content here..."
                    value={editingNote.content}
                    onChange={(e) => setEditingNote({ ...editingNote, content: e.target.value })}
                    rows={6}
                    className="transition-smooth focus:shadow-primary/20"
                  />
                </div>
                <Button onClick={handleUpdateNote} variant="gradient" className="w-full">
                  Update Note
                </Button>
              </div>
            )}
          </DialogContent>
        </Dialog>
      </div>
    </div>
  );
};

export default Notes;