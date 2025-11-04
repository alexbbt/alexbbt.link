import { redirect } from 'next/navigation';

export default function Home() {
  // Redirect root to admin interface
  redirect('/admin');
}
